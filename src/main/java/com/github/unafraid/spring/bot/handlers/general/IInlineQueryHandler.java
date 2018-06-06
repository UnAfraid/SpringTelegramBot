package com.github.unafraid.spring.bot.handlers.general;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * @author UnAfraid
 */
public interface IInlineQueryHandler {
	/**
	 * Fired whenever bot receives a callback query
	 *
	 * @param bot    the bot
	 * @param update the update
	 * @param query  the query
	 * @return {@code true} whenever this even has to be consumed, {@code false} to continue notified other handlers
	 * @throws TelegramApiException the exception
	 */
	boolean onInlineQuery(AbsSender bot, Update update, InlineQuery query) throws TelegramApiException;
}
