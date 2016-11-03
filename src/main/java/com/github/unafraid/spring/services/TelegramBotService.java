package com.github.unafraid.spring.services;

import com.github.unafraid.spring.bot.handlers.CommandHandler;
import com.github.unafraid.spring.bot.handlers.impl.ICommandHandler;
import com.github.unafraid.spring.bot.util.BotUtil;
import com.github.unafraid.spring.config.TelegramBotConfig;
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
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by UnAfraid on 21.10.2016 г..
 */
@Service
public class TelegramBotService extends TelegramWebhookBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotService.class);
    private static final Pattern COMMAND_ARGS_PATTERN = Pattern.compile("\"([^\"]*)\"|([^\\s]+)");

    @Inject
    private TelegramBotConfig config;

    @Inject
    private UsersService usersService;

    @Inject
    private void setWebHook(TelegramBotConfig config) throws Exception {
        try {
            final Map<String, String> params = new HashMap<>();
            params.put(SetWebhook.URL_FIELD, config.getPath());
            final URL urlAddress = new URL(Constants.BASEURL + getBotToken() + "/" + SetWebhook.PATH);
            final HttpURLConnection connection = (HttpURLConnection) urlAddress.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            try (final OutputStream os = connection.getOutputStream();
                 final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8.name()))) {

                final StringBuilder result = new StringBuilder();
                boolean first = true;
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if (first) {
                        first = false;
                    } else {
                        result.append("&");
                    }

                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                }

                writer.write(result.toString());
            }

            connection.connect();

            try (final InputStreamReader input = new InputStreamReader(connection.getInputStream());
                 final BufferedReader reader = new BufferedReader(input)) {
                final String responseContent = reader.lines().parallel().collect(Collectors.joining(System.lineSeparator()));
                JSONObject jsonObject = new JSONObject(responseContent);
                if (!jsonObject.getBoolean(Constants.RESPONSEFIELDOK)) {
                    throw new TelegramApiRequestException("Error setting webhook", jsonObject);
                }
            }

            connection.disconnect();
        } catch (JSONException e) {
            throw new TelegramApiRequestException("Error de-serializing setWebhook method response", e);
        } catch (IOException e) {
            throw new TelegramApiRequestException("Error executing setWebook method", e);
        }
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update != null) {
            try {
                if (update.hasChosenInlineQuery()) {
                    // TODO: handleChosenInlineQuery
                    LOGGER.warn("ChosenInlineQuery is not handled yet: Update: {}", update);
                } else if (update.hasInlineQuery()) {
                    // TODO: handleInlineQuery
                    LOGGER.warn("InlineQuery is not handled yet: Update: {}", update);
                } else if (update.hasEditedMessage()) {
                    // TODO: handleEditedMessage
                    LOGGER.warn("EditedMessage is not handled yet: Update: {}", update);
                } else if (update.hasCallbackQuery()) {
                    handleIncomingCallQuery(update);
                } else if (update.hasMessage()) {
                    final Message message = update.getMessage();
                    if (message.hasText()) {
                        handleIncomingMessage(update, message);
                    } else {
                        LOGGER.warn("Message doesn't have text Update: {}", update);
                    }
                } else {
                    LOGGER.warn("Update doesn't contains neither ChosenInlineQuery/InlineQuery/CallbackQuery/EditedMessage/Message Update: {}", update);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to handle incoming update", e);
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
                if (!usersService.validate(query.getFrom().getId(), commandHandler.getRequiredAccessLevel())) {
                    continue;
                }

                if (commandHandler.onCallbackQuery(this, update, query)) {
                    break;
                }
            } catch (TelegramApiRequestException e) {
                LOGGER.warn("Exception caught on handler: {} error: {}", commandHandler.getClass().getSimpleName(), e.getApiResponse(), e);
            } catch (TelegramApiException e) {
                LOGGER.warn("Exception caught on handler: {}", commandHandler.getClass().getSimpleName(), e);
            }
        }
    }

    private void handleIncomingMessage(Update update, Message message) {
        String text = message.getText();
        if (text == null) {
            return;
        }

        // Parse commands that goes like: @BotNickname help to /help
        if (text.startsWith("@" + getBotUsername() + " ")) {
            text = '/' + text.substring(("@" + getBotUsername() + " ").length());
        }
        // Parse commands that goes like: /help@BotNickname to /help
        else if (text.contains("@" + getBotUsername())) {
            text = text.replaceAll("@" + getBotUsername(), "");
            if (text.charAt(0) != '/') {
                text = '/' + text;
            }
        }

        // Parse arguments to a list
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
                    if (!usersService.validate(message.getFrom().getId(), handler.getRequiredAccessLevel())) {
                        BotUtil.sendMessage(this, message, message.getFrom().getUserName() + ": You are not authorized to use this function!", true, false, null);
                        return;
                    }

                    handler.onCommandMessage(this, update, message, args);
                } catch (TelegramApiRequestException e) {
                    LOGGER.warn("API Exception caught on handler: {}, response: {} message: {}", handler.getClass().getSimpleName(), e.getApiResponse(), message, e);
                } catch (Exception e) {
                    LOGGER.warn("Exception caught on handler: {}, message: {}", handler.getClass().getSimpleName(), message, e);
                }
            } else {
                for (ICommandHandler commandHandler : CommandHandler.getInstance().getHandlers()) {
                    try {
                        if (!usersService.validate(message.getFrom().getId(), commandHandler.getRequiredAccessLevel())) {
                            continue;
                        }

                        if (commandHandler.onMessage(this, update, message, args)) {
                            break;
                        }
                    } catch (TelegramApiRequestException e) {
                        LOGGER.warn("API Exception caught on handler: {}, response: {} message: {}", commandHandler.getClass().getSimpleName(), e.getApiResponse(), message, e);
                    } catch (Exception e) {
                        LOGGER.warn("Exception caught on handler: {}, message: {}", commandHandler.getClass().getSimpleName(), message, e);
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
