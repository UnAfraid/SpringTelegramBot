package com.github.unafraid.spring.config;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by UnAfraid on 22.10.2016 г..
 */
@ConfigurationProperties(prefix = "telegram")
public class TelegramBotConfig {
    @NotBlank
    private String token;

    @NotBlank
    private String username;

    @NotBlank
    private String path;

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public String getPath() {
        return path;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AnnotationConfiguration{");
        sb.append("token='").append(token).append('\'');
        sb.append(", username='").append(username).append('\'');
        sb.append(", path='").append(path).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
