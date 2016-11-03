package com.github.unafraid.spring.bot.handlers.general;

import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * @author UnAfraid
 */
public interface IMessageHandler {
    /**
     * Fired whenever user types anything but a command
     *
     * @param bot     the bot
     * @param update  the update
     * @param message the message
     * @throws TelegramApiException the exception
     */
    boolean onMessage(AbsSender bot, Update update, Message message) throws TelegramApiException;
}
