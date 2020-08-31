package com.github.unafraid.spring.services;

import com.github.unafraid.spring.bot.AccessLevelValidator;
import com.github.unafraid.spring.bot.TelegramWebHookBot;
import com.github.unafraid.spring.config.TelegramBotConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.ApiResponse;
import org.telegram.telegrambots.meta.api.objects.WebhookInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

/**
 * @author UnAfraid
 */
@Service
public class TelegramWebHookBotService extends TelegramWebHookBot {
    private final String webPath;

    public TelegramWebHookBotService(TelegramBotConfig config, ApplicationContext appContext, AccessLevelValidator accessLevelValidator) throws Exception {
        super(config.getToken(), config.getUsername(), appContext, accessLevelValidator);
        this.webPath = config.getUrl();
        final WebhookInfo info = getWebhookInfo();
        final String url = info.getUrl();
        final StringBuilder sb = new StringBuilder(config.getUrl());
        if (sb.charAt(sb.length() - 1) != '/') {
            sb.append('/');
        }
        sb.append("callback/");
        sb.append(config.getToken());
        final String webHookUrl = sb.toString();
        if (url == null || url.isEmpty() || !url.equals(webHookUrl)) {
            setWebhook(webHookUrl, "");
        }
    }

    @Override
    public void setWebhook(String url, String publicCertificatePath) throws TelegramApiRequestException {
        try {
            final SetWebhook setWebhook = new SetWebhook();
            setWebhook.setUrl(url);
            if (publicCertificatePath != null && !publicCertificatePath.isEmpty()) {
                setWebhook.setCertificateFile(publicCertificatePath);
            }
            setWebhook.setMaxConnections(40);

            final RestTemplate rest = new RestTemplate();
            final HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("Accept", "*/*");

            final String setWebhookUrl = String.format("https://api.telegram.org/bot%s/%s", getBotToken(), SetWebhook.PATH);
            rest.exchange(setWebhookUrl, HttpMethod.POST, new HttpEntity<>(setWebhook, headers), ApiResponse.class);
        } catch (Exception e) {
            throw new TelegramApiRequestException("Error executing setWebHook method", e);
        }
    }

    @Override
    public String getBotPath() {
        return webPath;
    }
}
