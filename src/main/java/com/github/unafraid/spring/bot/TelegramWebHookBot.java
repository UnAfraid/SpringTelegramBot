package com.github.unafraid.spring.bot;

import java.util.List;
import java.util.Map;

import com.github.unafraid.telegrambot.bots.DefaultTelegramBot;
import com.github.unafraid.telegrambot.handlers.ICommandHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * @author UnAfraid
 */
public abstract class TelegramWebHookBot extends DefaultTelegramBot  {
	public TelegramWebHookBot(@NotNull String token,
							  @NotNull ApplicationContext appContext,
							  @NotNull ObjectProvider<TelegramClient> telegramClientProvider,
							  AccessLevelValidator accessLevelValidator) {
		super(telegramClientProvider.getIfAvailable(() -> new OkHttpTelegramClient(token)));
		
		setAccessLevelValidator(accessLevelValidator);
		
		final Map<String, ICommandHandler> handlers = appContext.getBeansOfType(ICommandHandler.class);
		handlers.values().forEach(this::addHandler);
	}
	
	public final void onWebhookUpdateReceived(Update update) {
		if (update != null) {
			consume(List.of(update));
		}
	}
}
