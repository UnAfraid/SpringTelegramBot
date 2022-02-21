package com.github.unafraid.spring.bot.handlers.impl;

import java.util.List;

import com.github.unafraid.telegrambot.bots.AbstractTelegramBot;
import com.github.unafraid.telegrambot.handlers.ICommandHandler;
import com.github.unafraid.telegrambot.util.BotUtil;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * @author UnAfraid
 */
@Service
public final class StartHandler implements ICommandHandler {
	public StartHandler() {
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
		BotUtil.sendMessage(bot, message, "Hello, i am " + bot.getMe().getUserName() + ", if you want to know what i can do type /start", true, false, null);
	}
}
