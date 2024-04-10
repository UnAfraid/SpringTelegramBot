package com.github.unafraid.spring.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * @author UnAfraid
 */
@Configuration
@Validated
public class TelegramBotConfig {
	@Value("${TELEGRAM_TOKEN}")
	@NotNull
	private String token;
	
	@Value("${TELEGRAM_URL}")
	@NotNull
	private String url;
	
	@Value("${TELEGRAM_MAX_CONNECTIONS:40}")
	@NotNull
	private Integer maxConnections;
	
	@Value("${TELEGRAM_LANGUAGE_CODE:en}")
	@NotNull
	private String languageCode;
	
	public String getToken() {
		return token;
	}
	
	public String getUrl() {
		return url;
	}
	
	public int getMaxConnections() {
		return maxConnections;
	}
	
	public String getLanguageCode() {
		return languageCode;
	}
}
