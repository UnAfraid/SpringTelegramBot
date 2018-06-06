package com.github.unafraid.spring.bot.handlers.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import com.github.unafraid.spring.bot.util.BotUtil;
import com.github.unafraid.telegrambot.handlers.ICommandHandler;

/**
 * @author UnAfraid
 */
@Service
public final class WhoAmI implements ICommandHandler {
	@Override
	public String getCommand() {
		return "/whoami";
	}

	@Override
	public String getUsage() {
		return "/whoami";
	}

	@Override
	public String getDescription() {
		return "Shows information for the user who types the command";
	}

	@Override
	public void onCommandMessage(AbsSender bot, Update update, Message message, List<String> args) throws TelegramApiException {
		final StringBuilder sb = new StringBuilder();
		sb.append("Your id: ").append(message.getFrom().getId()).append(System.lineSeparator());
		sb.append("Name: ").append(message.getFrom().getFirstName()).append(System.lineSeparator());
		if (message.getFrom().getUserName() != null) {
			sb.append("Username: @").append(message.getFrom().getUserName()).append(System.lineSeparator());
		}
		sb.append("Chat Type: ").append(message.getChat().isGroupChat() ? "Group Chat" : message.getChat().isSuperGroupChat() ? "Super Group Chat" : message.getChat().isChannelChat() ? "Channel Chat" : message.getChat().isUserChat() ? "DBUser Chat" : "No way!?").append(System.lineSeparator());
		if (message.getChat().getId() < 0) {
			sb.append("Group Id: ").append(message.getChat().getId()).append(System.lineSeparator());
			sb.append("Group Name: ").append(message.getChat().getTitle()).append(System.lineSeparator());
		}
		BotUtil.sendMessage(bot, message, sb.toString(), true, false, null);
	}
}
