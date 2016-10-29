package com.github.unafraid.spring.bot.handlers.impl;

import com.github.unafraid.spring.bot.util.BotUtil;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.List;

/**
 * @author UnAfraid
 */
public final class StartHandler implements ICommandHandler {
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
        BotUtil.sendMessage(bot, message, "Hello, i am " + bot.getMe().getUserName() + ", if you want to know what i can do type /start", true, false, null);
    }
}
