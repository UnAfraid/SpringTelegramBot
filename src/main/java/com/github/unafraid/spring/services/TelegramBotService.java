package com.github.unafraid.spring.services;

import com.github.unafraid.spring.bot.handlers.CommandHandler;
import com.github.unafraid.spring.bot.handlers.impl.ICommandHandler;
import com.github.unafraid.spring.bot.util.BotUtil;
import com.github.unafraid.spring.config.TelegramBotConfig;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.Constants;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by UnAfraid on 21.10.2016 г..
 */
@Service
public class TelegramBotService extends TelegramWebhookBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotService.class);
    private static final int SOCKET_TIMEOUT = 10 * 1000;
    private static final Pattern COMMAND_ARGS_PATTERN = Pattern.compile("\"([^\"]*)\"|([^\\s]+)");

    private TelegramBotConfig config;

    @Inject
    private UsersService usersService;

    @Inject
    public TelegramBotService(TelegramBotConfig config) {
        this.config = config;
        try {
            registerWebHook();
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    private void registerWebHook() throws TelegramApiRequestException {
        try (CloseableHttpClient httpclient = HttpClientBuilder.create().setSSLHostnameVerifier(new NoopHostnameVerifier()).build()) {
            final String url = Constants.BASEURL + getBotToken() + "/" + SetWebhook.PATH;

            final RequestConfig.Builder configBuilder = RequestConfig.copy(RequestConfig.custom().build())
                    .setSocketTimeout(SOCKET_TIMEOUT)
                    .setConnectTimeout(SOCKET_TIMEOUT)
                    .setConnectionRequestTimeout(SOCKET_TIMEOUT);

            final HttpPost post = new HttpPost(url);
            post.setConfig(configBuilder.build());
            final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody(SetWebhook.URL_FIELD, getBotPath());
            //  if (publicCertificatePath != null) {
            //      File certificate = new File(publicCertificatePath);
            //      if (certificate.exists()) {
            //          builder.addBinaryBody(SetWebhook.CERTIFICATE_FIELD, certificate, ContentType.TEXT_PLAIN, certificate.getName());
            //      }
            //  }
            post.setEntity(builder.build());
            try (CloseableHttpResponse response = httpclient.execute(post)) {
                final HttpEntity ht = response.getEntity();
                final BufferedHttpEntity buf = new BufferedHttpEntity(ht);
                final String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                final JSONObject jsonObject = new JSONObject(responseContent);
                if (!jsonObject.getBoolean(Constants.RESPONSEFIELDOK)) {
                    throw new TelegramApiRequestException(getBotPath() == null ? "Error removing old webhook" : "Error setting webhook", jsonObject);
                }
                LOGGER.info("Response from telegram api: {}", responseContent);
            }
        } catch (JSONException e) {
            throw new TelegramApiRequestException("Error deserializing setWebhook method response", e);
        } catch (IOException e) {
            throw new TelegramApiRequestException("Error executing setWebook method", e);
        }
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        if (update != null) {
            try {
                if (update.hasCallbackQuery()) {
                    handleIncomingCallQuery(update);
                } else {
                    final Message message = update.getMessage();
                    if (message != null) {
                        if (message.hasText()) {
                            handleIncomingMessage(update.getUpdateId(), message);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to handle incomming update", e);
            }
        }
        return null;
    }

    private void handleIncomingCallQuery(Update update) {
        final CallbackQuery query = update.getCallbackQuery();
        if (query == null) {
            return;
        }

        for (ICommandHandler commandHandler : CommandHandler.getInstance().getHandlers()) {
            try {
                final int id = query.getFrom().getId();
                if (!usersService.validate(id, commandHandler.getRequiredAccessLevel())) {
                    continue;
                }

                if (commandHandler.onCallback(this, update, query)) {
                    break;
                }
            } catch (TelegramApiRequestException e) {
                LOGGER.warn("Exception caught on handler: {} error: {}", commandHandler.getClass().getSimpleName(), e.getApiResponse(), e);
            } catch (TelegramApiException e) {
                LOGGER.warn("Exception caught on handler: {}", commandHandler.getClass().getSimpleName(), e);
            }
        }
    }

    private void handleIncomingMessage(int updateId, Message message) {
        String text = message.getText();
        if (text == null) {
            return;
        }

        if (text.startsWith("@" + getBotUsername() + " ")) {
            text = '/' + text.substring(("@" + getBotUsername() + " ").length());
        } else if (text.contains("@" + getBotUsername())) {
            text = text.replaceAll("@" + getBotUsername(), "");
            if (text.charAt(0) != '/') {
                text = '/' + text;
            }
        }

        final Matcher matcher = COMMAND_ARGS_PATTERN.matcher(text);
        if (matcher.find()) {
            String command = matcher.group();
            final List<String> args = new ArrayList<>();
            String arg;

            while (matcher.find()) {
                arg = matcher.group(1);
                if (arg == null) {
                    arg = matcher.group(0);
                }

                args.add(arg);
            }

            final ICommandHandler handler = CommandHandler.getInstance().getHandler(command);
            if (handler != null) {
                try {
                    final int id = message.getFrom().getId();
                    if (!usersService.validate(id, handler.getRequiredAccessLevel())) {
                        BotUtil.sendMessage(this, message, message.getFrom().getUserName() + ": You are not authorized to use this function!", true, false, null);
                        return;
                    }

                    handler.onMessage(this, message, updateId, args);
                } catch (TelegramApiRequestException e) {
                    LOGGER.warn("API Exception caught on handler: {}, response: {} message: {}", handler.getClass().getSimpleName(), e.getApiResponse(), message, e);
                } catch (Exception e) {
                    LOGGER.warn("Exception caught on handler: {}, message: {}", handler.getClass().getSimpleName(), message, e);
                }
            } else {
                for (ICommandHandler commandHandler : CommandHandler.getInstance().getHandlers()) {
                    try {
                        final int id = message.getFrom().getId();
                        if (!usersService.validate(id, commandHandler.getRequiredAccessLevel())) {
                            continue;
                        }

                        if (commandHandler.onMessage(this, message, args)) {
                            break;
                        }
                    } catch (TelegramApiRequestException e) {
                        LOGGER.warn("API Exception caught on handler: {}, response: {} message: {}", handler.getClass().getSimpleName(), e.getApiResponse(), message, e);
                    } catch (Exception e) {
                        LOGGER.warn("Exception caught on handler: {}, message: {}", handler.getClass().getSimpleName(), message, e);
                    }
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return config.getUsername();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public String getBotPath() {
        return config.getPath();
    }
}
