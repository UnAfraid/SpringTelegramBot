package com.github.unafraid.spring.bot.handlers.impl;

import com.github.unafraid.spring.services.UserService;
import com.github.unafraid.telegrambot.bots.AbstractTelegramBot;
import com.github.unafraid.telegrambot.handlers.ICommandHandler;
import com.github.unafraid.telegrambot.util.BotUtil;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author UnAfraid
 */
@Service
public final class StartHandler implements ICommandHandler {
    private final UserService userService;

    private final AtomicBoolean createdAdmin = new AtomicBoolean();

    public StartHandler(UserService userService) {
        this.userService = userService;
    }

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
    public void onCommandMessage(AbstractTelegramBot bot, Update update, Message message, List<String> args) throws TelegramApiException {
        if (!createdAdmin.get() && userService.findAll().isEmpty()) {
            // In case there aren't any users create the first who wrote to the bot as admin and mark as created
            if (createdAdmin.compareAndSet(false, true)) {
                userService.create(message.getFrom().getId(), message.getFrom().getUserName(), 10);
                BotUtil.sendMessage(bot, message, "Hello master, i am " + bot.getMe().getUserName() + ", if you want to know what i can do type /start", true, false, null);
            }
        } else {
            // In case there's already an admin we won't fetch all users
            createdAdmin.set(true);
            BotUtil.sendMessage(bot, message, "Hello, i am " + bot.getMe().getUserName() + ", if you want to know what i can do type /start", true, false, null);
        }
    }
}
