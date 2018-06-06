package com.github.unafraid.spring.bot.handlers.impl;

import java.net.InetAddress;
import java.util.List;
import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import com.github.unafraid.spring.services.UsersService;
import com.github.unafraid.telegrambot.handlers.IAccessLevelHandler;
import com.github.unafraid.telegrambot.handlers.ICommandHandler;
import com.github.unafraid.telegrambot.util.BotUtil;


/**
 * @author UnAfraid
 */
@Service
public final class ResolveHandler implements ICommandHandler, IAccessLevelHandler {
	private UsersService usersService;

	@Inject
	public ResolveHandler(UsersService usersService) {
		this.usersService = usersService;
	}

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
	public boolean validate(User from) {
		return usersService.validate(from.getId(), getRequiredAccessLevel());
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
