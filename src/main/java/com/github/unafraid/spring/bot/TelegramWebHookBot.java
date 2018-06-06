package com.github.unafraid.spring.bot;

import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.generics.WebhookBot;
import com.github.unafraid.telegrambot.bots.DefaultTelegramBot;

/**
 * @author UnAfraid
 */
public abstract class TelegramWebHookBot extends DefaultTelegramBot implements WebhookBot {
	public TelegramWebHookBot(String token, String username) {
		super(token, username);
	}

	@Override
	public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
		System.out.println("Update arrived: " + update);
		if (update != null) {
			onUpdateReceived(update);
		}
		return null;
	}
}
