package com.github.unafraid.spring.bot.handlers.impl;

import com.github.unafraid.telegrambot.bots.AbstractTelegramBot;
import com.github.unafraid.telegrambot.handlers.ICommandHandler;
import com.github.unafraid.telegrambot.util.BotUtil;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.InetAddress;
import java.util.List;


/**
 * @author UnAfraid
 */
@Service
public final class ResolveHandler implements ICommandHandler {
    @Override
    public String getCommand() {
        return "/resolve";
    }

    @Override
    public String getUsage() {
        return "/resolve <host>";
    }

    @Override
    public String getDescription() {
        return "Resolved hostname to ip address";
    }

    @Override
    public String getCategory() {
        return "Utilities";
    }

    @Override
    public int getRequiredAccessLevel() {
        return 1;
    }

    @Override
    public void onCommandMessage(AbstractTelegramBot bot, Update update, Message message, List<String> args) throws TelegramApiException {
        if (args.isEmpty()) {
            BotUtil.sendUsage(bot, message, this);
            return;
        }
        final String hostName = args.get(0);
        try {
            final InetAddress address = InetAddress.getByName(hostName);
            BotUtil.sendMessage(bot, message, "*" + hostName + "* = " + address.getHostAddress(), true, true, null);
        } catch (Exception e) {
            BotUtil.sendMessage(bot, message, "Failed to resolve: " + hostName + " " + e.getMessage(), true, false, null);
        }
    }
}
