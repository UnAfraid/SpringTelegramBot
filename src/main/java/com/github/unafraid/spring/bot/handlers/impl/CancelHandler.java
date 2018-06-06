package com.github.unafraid.spring.bot.handlers.impl;

import java.util.List;
import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import com.github.unafraid.spring.bot.handlers.general.CommandHandler;
import com.github.unafraid.spring.bot.handlers.general.ICancelHandler;
import com.github.unafraid.spring.bot.handlers.general.ICommandHandler;
import com.github.unafraid.spring.services.UsersService;

/**
 * @author UnAfraid
 */
@Service
public class CancelHandler implements ICommandHandler {

	@Inject
	private UsersService usersService;

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
		for (ICancelHandler handler : CommandHandler.getInstance().getHandlers(ICancelHandler.class, message.getFrom().getId(), usersService)) {
			handler.onCancel(bot, update, message);
		}
	}
}
