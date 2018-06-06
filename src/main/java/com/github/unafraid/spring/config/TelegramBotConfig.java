package com.github.unafraid.spring.config;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author UnAfraid
 */
@ConfigurationProperties(prefix = "telegram")
@Validated
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
}
