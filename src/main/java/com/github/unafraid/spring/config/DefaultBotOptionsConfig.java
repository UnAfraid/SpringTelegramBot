package com.github.unafraid.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;

/**
 * @author UnAfraid
 */
@Configuration
public class DefaultBotOptionsConfig {
	@Bean
	public DefaultBotOptions newDefaultBotOptions() {
		final DefaultBotOptions defaultBotOptions = new DefaultBotOptions();
		return defaultBotOptions;
	}
}
