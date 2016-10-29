package com.github.unafraid.spring.bot.handlers.impl;

import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.List;

/**
 * @author UnAfraid
 */
public interface ICommandHandler {
    String getCommand();

    String getUsage();

    String getDescription();

    default int getRequiredAccessLevel() {
        return 0;
    }

    default String getCategory() {
        return getRequiredAccessLevel() > 0 ? "Admin commands" : "Public commands";
    }

    void onMessage(AbsSender bot, Message message, int updateId, List<String> args) throws TelegramApiException;

    default boolean onMessage(AbsSender bot, Message message, List<String> args) throws TelegramApiException {
        return false;
    }

    default boolean onCallback(AbsSender bot, Update update, CallbackQuery query) throws TelegramApiException {
        return false;
    }

    default void onCancel(AbsSender bot, Message message) throws TelegramApiException {

    }
}
