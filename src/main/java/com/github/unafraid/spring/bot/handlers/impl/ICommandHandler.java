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
    /**
     * @return The command that will trigger @{link onCommandMessage} method
     */
    String getCommand();

    /**
     * @return The usage of the command whenever user types in /command without parameters some commands may return that if requires arguments to be supplied
     */
    String getUsage();

    /**
     * @return The description of the command shown in /help
     */
    String getDescription();

    /**
     * @return The access level required to execute this command
     */
    default int getRequiredAccessLevel() {
        return 0;
    }

    /**
     * @return The category mapping whenever u type in /help that would group current command to the returned category
     */
    default String getCategory() {
        return getRequiredAccessLevel() > 0 ? "Admin commands" : "Public commands";
    }

    /**
     * Fired when user types in /command arg0 arg1 arg2..
     *
     * @param bot     the bot
     * @param update  the update
     * @param message the message
     * @param args    the arguments after command separated by space or wrapped within "things here are considered one arg"
     * @throws TelegramApiException the exception
     */
    void onCommandMessage(AbsSender bot, Update update, Message message, List<String> args) throws TelegramApiException;

    /**
     * Fired whenever user types anything but a command
     *
     * @param bot     the bot
     * @param update  the update
     * @param message the message
     * @param args    the arguments after command separated by space or wrapped within "things here are considered one arg"   @return
     * @throws TelegramApiException the exception
     */
    default boolean onMessage(AbsSender bot, Update update, Message message, List<String> args) throws TelegramApiException {
        return false;
    }

    /**
     * Fired whenever bot receives a callback query
     *
     * @param bot    the bot
     * @param update the update
     * @param query  the query
     * @return {@code true} whenever this even has to be consumed, {@code false} to continue notified other handlers
     * @throws TelegramApiException the exception
     */
    default boolean onCallbackQuery(AbsSender bot, Update update, CallbackQuery query) throws TelegramApiException {
        return false;
    }

    /**
     * Fired whenever user types in /cancel command to cancel the current action
     *
     * @param bot     the bot
     * @param update  the update
     * @param message the message
     * @throws TelegramApiException the exception
     */
    default void onCancel(AbsSender bot, Update update, Message message) throws TelegramApiException {

    }
}
