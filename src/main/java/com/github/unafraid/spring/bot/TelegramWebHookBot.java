package com.github.unafraid.spring.bot;

import com.github.unafraid.telegrambot.bots.DefaultTelegramBot;
import com.github.unafraid.telegrambot.handlers.ICommandHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

/**
 * @author UnAfraid
 */
public class TelegramWebHookBot extends DefaultTelegramBot {
	public TelegramWebHookBot(@NotNull String token,
							  @NotNull ApplicationContext appContext,
							  @NotNull ObjectProvider<TelegramClient> telegramClientProvider,
							  AccessLevelValidator accessLevelValidator) {
		super(telegramClientProvider.getIfAvailable(() -> new OkHttpTelegramClient(token)));
		
		setAccessLevelValidator(accessLevelValidator);
	}

	/**
	 * Register command handlers with the underlying bot. Call this after the Spring
	 * context is fully initialized (for example, from an ApplicationReadyEvent listener).
	 */
	public void registerHandlers(Iterable<ICommandHandler> handlers) {
		if (handlers == null) return;
		for (ICommandHandler h : handlers) {
			addHandler(h);
		}
	}
	
	public final void onWebhookUpdateReceived(Update update) {
		if (update != null) {
			consume(List.of(update));
		}
	}
}
