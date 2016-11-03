package com.github.unafraid.spring.bot.handlers.general;

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
     * @return The category mapping whenever u type in /help that would group current command to the returned category
     */
    default String getCategory() {
        if (this instanceof IAccessLevelHandler) {
            IAccessLevelHandler accessLevelHandler = (IAccessLevelHandler) this;
            return (accessLevelHandler.getRequiredAccessLevel() > 0) ? "Admin commands" : "Public commands";
        }
        return "Public commands";
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
}
