package com.github.unafraid.spring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author UnAfraid
 */
@Configuration
@Validated
public class TelegramBotConfig {
    @Value("${TELEGRAM_TOKEN}")
    @NotNull
    @NotEmpty
    private String token;

    @Value("${TELEGRAM_USERNAME}")
    @NotNull
    @NotEmpty
    private String username;

    @Value("${TELEGRAM_URL}")
    @NotNull
    @NotEmpty
    private String url;

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public String getUrl() {
        return url;
    }
}
