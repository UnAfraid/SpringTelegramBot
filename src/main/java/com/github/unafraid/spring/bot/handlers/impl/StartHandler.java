package com.github.unafraid.spring.bot.handlers.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import com.github.unafraid.spring.bot.util.BotUtil;
import com.github.unafraid.spring.services.UsersService;
import com.github.unafraid.telegrambot.handlers.ICommandHandler;

/**
 * @author UnAfraid
 */
@Service
public final class StartHandler implements ICommandHandler {
	@Inject
	private UsersService usersService;

	private final AtomicBoolean createdAdmin = new AtomicBoolean();

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
	public void onCommandMessage(AbsSender bot, Update update, Message message, List<String> args) throws TelegramApiException {
		if (!createdAdmin.get() && usersService.findAll().isEmpty()) {
			// In case there aren't any users create the first who wrote to the bot as admin and mark as created
			if (createdAdmin.compareAndSet(false, true)) {
				usersService.create(message.getFrom().getId(), message.getFrom().getUserName(), 10);
				BotUtil.sendMessage(bot, message, "Hello master, i am " + bot.getMe().getUserName() + ", if you want to know what i can do type /start", true, false, null);
			}
		} else {
			// In case there's already an admin we won't fetch all users
			createdAdmin.set(true);
			BotUtil.sendMessage(bot, message, "Hello, i am " + bot.getMe().getUserName() + ", if you want to know what i can do type /start", true, false, null);
		}
	}
}
