package com.github.unafraid.spring.bot;

import java.util.Map;

import com.github.unafraid.telegrambot.bots.DefaultTelegramBot;
import com.github.unafraid.telegrambot.handlers.ICommandHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.WebhookBot;

/**
 * @author UnAfraid
 */
public abstract class TelegramWebHookBot extends DefaultTelegramBot implements WebhookBot {
	public TelegramWebHookBot(@NotNull String token,
                              @NotNull String username,
							  @NotNull ApplicationContext appContext,
							  @NotNull ObjectProvider<DefaultBotOptions> defaultBotOptions,
							  AccessLevelValidator accessLevelValidator) {
		super(token, username, defaultBotOptions.getIfAvailable(DefaultBotOptions::new));
		
		setAccessLevelValidator(accessLevelValidator);
		
		final Map<String, ICommandHandler> handlers = appContext.getBeansOfType(ICommandHandler.class);
		handlers.values().forEach(this::addHandler);
	}
	
	@Override
	public final BotApiMethod<?> onWebhookUpdateReceived(Update update) {
		if (update != null) {
			onUpdateReceived(update);
		}
		return null;
	}
}
