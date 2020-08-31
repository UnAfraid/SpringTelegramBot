package com.github.unafraid.spring.bot.handlers.impl;

import com.github.unafraid.telegrambot.bots.AbstractTelegramBot;
import com.github.unafraid.telegrambot.handlers.ICommandHandler;
import com.github.unafraid.telegrambot.util.BotUtil;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author UnAfraid
 */
@Service
public final class HelpHandler implements ICommandHandler {
    @Override
    public String getCommand() {
        return "/help";
    }

    @Override
    public String getUsage() {
        return "/help [command]";
    }

    @Override
    public String getDescription() {
        return "Shows help for all or specific command";
    }

    @Override
    public void onCommandMessage(AbstractTelegramBot bot, Update update, Message message, List<String> args) throws TelegramApiException {
        if (args.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            final Map<String, List<String>> help = new LinkedHashMap<>();
            bot.getHandlers()
                    .stream()
                    .filter(handler -> bot.validateAccessLevel(handler, message.getFrom()))
                    .filter(handler -> handler instanceof ICommandHandler)
                    .map(handler -> (ICommandHandler) handler)
                    .forEach(handler -> help.computeIfAbsent(handler.getCategory(), key -> new ArrayList<>()).add(handler.getCommand() + " - " + handler.getDescription()));

            help.forEach((key, value) -> {
                sb.append(key).append(":").append(System.lineSeparator());
                for (String line : value) {
                    sb.append(line).append(System.lineSeparator());
                }
                sb.append(System.lineSeparator());
            });

            BotUtil.sendMessage(bot, message, sb.toString(), true, false, null);
            return;
        }

        String command = args.get(0);
        if (command.charAt(0) != '/') {
            command = '/' + command;
        }
        final ICommandHandler handler = bot.getHandler(command);
        if (handler == null) {
            BotUtil.sendMessage(bot, message, "Unknown command.", false, false, null);
            return;
        }

        BotUtil.sendMessage(bot, message, "Usage:" + System.lineSeparator() + handler.getUsage(), true, false, null);
    }
}
