package com.github.unafraid.spring.bot.handlers.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import com.github.unafraid.spring.bot.handlers.general.CommandHandler;
import com.github.unafraid.spring.bot.handlers.general.IAccessLevelHandler;
import com.github.unafraid.spring.bot.handlers.general.ICommandHandler;
import com.github.unafraid.spring.bot.util.BotUtil;
import com.github.unafraid.spring.services.UsersService;

/**
 * @author UnAfraid
 */
@Service
public final class HelpHandler implements ICommandHandler {
	@Inject
	private UsersService usersService;

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
	public void onCommandMessage(AbsSender bot, Update update, Message message, List<String> args) throws TelegramApiException {
		if (args.isEmpty()) {
			final StringBuilder sb = new StringBuilder();
			final Map<String, List<String>> help = new LinkedHashMap<>();
			CommandHandler.getInstance().getHandlers()
					.stream()
					.filter(handler -> IAccessLevelHandler.validate(handler, message.getFrom().getId(), usersService))
					.forEach(handler -> help.computeIfAbsent(handler.getCategory(), key -> new ArrayList<>()).add(handler.getCommand() + " - " + handler.getDescription()));

			help.entrySet().forEach(entry ->
			{
				sb.append(entry.getKey()).append(":").append(System.lineSeparator());
				for (String line : entry.getValue()) {
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
		final ICommandHandler handler = CommandHandler.getInstance().getHandler(command);
		if (handler == null) {
			BotUtil.sendMessage(bot, message, "Unknown command.", false, false, null);
			return;
		}

		//if (UsersHandler.validate(id, handler.getRequiredAccessLevel())) {
		BotUtil.sendMessage(bot, message, "Usage:" + System.lineSeparator() + handler.getUsage(), true, false, null);
		//}
	}
}
