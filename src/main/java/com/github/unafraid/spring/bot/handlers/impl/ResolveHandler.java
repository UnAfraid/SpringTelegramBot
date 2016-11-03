package com.github.unafraid.spring.bot.handlers.impl;

import com.github.unafraid.spring.bot.handlers.general.ICommandHandler;
import com.github.unafraid.spring.bot.util.BotUtil;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;

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
    public void onCommandMessage(AbsSender bot, Update update, Message message, List<String> args) throws TelegramApiException {
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
