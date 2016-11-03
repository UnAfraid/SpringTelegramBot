package com.github.unafraid.spring.bot.handlers.impl;

import com.github.unafraid.spring.bot.handlers.CommandHandler;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;

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
    public void onCommandMessage(AbsSender bot, Update update, Message message, List<String> args) throws TelegramApiException {
        for (ICommandHandler handler : CommandHandler.getInstance().getHandlers()) {
            handler.onCancel(bot, update, message);
        }
    }
}
