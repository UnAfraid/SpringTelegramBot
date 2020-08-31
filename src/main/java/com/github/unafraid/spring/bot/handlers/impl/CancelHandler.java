package com.github.unafraid.spring.bot.handlers.impl;

import com.github.unafraid.telegrambot.bots.AbstractTelegramBot;
import com.github.unafraid.telegrambot.handlers.ICancelHandler;
import com.github.unafraid.telegrambot.handlers.ICommandHandler;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

/**
 * @author UnAfraid
 */
@Service
public class CancelHandler implements ICommandHandler {
    @Override
    public String getCommand() {
        return "/cancel";
    }

    @Override
    public String getUsage() {
        return "/cancel";
    }

    @Override
    public String getDescription() {
        return "Cancels current action";
    }

    @Override
    public void onCommandMessage(AbstractTelegramBot bot, Update update, Message message, List<String> args) throws TelegramApiException {
        for (ICancelHandler handler : bot.getAvailableHandlersForUser(ICancelHandler.class, message.getFrom())) {
            handler.onCancel(bot, update, message);
        }
    }
}
