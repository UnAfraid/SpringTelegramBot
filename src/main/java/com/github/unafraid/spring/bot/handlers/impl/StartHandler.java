package com.github.unafraid.spring.bot.handlers.impl;

import com.github.unafraid.spring.bot.db.services.IUsersService;
import com.github.unafraid.spring.bot.util.BotUtil;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import javax.inject.Inject;
import java.util.List;

/**
 * @author UnAfraid
 */
@Service
public final class StartHandler implements ICommandHandler {
    @Inject
    private IUsersService usersService;

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public String getUsage() {
        return "/start";
    }

    @Override
    public String getDescription() {
        return "Shows greetings message";
    }

    @Override
    public void onMessage(AbsSender bot, Message message, int updateId, List<String> args) throws TelegramApiException {
        if (usersService.findAll().isEmpty()) {
            usersService.create(message.getFrom().getId(), message.getFrom().getUserName(), 10);
            BotUtil.sendMessage(bot, message, "Hello master, i am " + bot.getMe().getUserName() + ", if you want to know what i can do type /start", true, false, null);
        } else {
            BotUtil.sendMessage(bot, message, "Hello, i am " + bot.getMe().getUserName() + ", if you want to know what i can do type /start", true, false, null);
        }
    }
}
