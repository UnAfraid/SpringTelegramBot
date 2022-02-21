package com.github.unafraid.spring.bot.handlers.impl;

import com.github.unafraid.telegrambot.handlers.inline.AbstractInlineHandler;
import com.github.unafraid.telegrambot.handlers.inline.InlineButtonBuilder;
import com.github.unafraid.telegrambot.handlers.inline.InlineContext;
import com.github.unafraid.telegrambot.handlers.inline.InlineMenuBuilder;
import com.github.unafraid.telegrambot.handlers.inline.InlineUserData;
import com.github.unafraid.telegrambot.handlers.inline.events.InlineCallbackEvent;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Very basic inline menu handler, accepts command starting with /menu
 */
@Service
public class ExampleInlineMenuHandler extends AbstractInlineHandler {
	@Override
	public String getUsage() {
		return "/menu";
	}
	
	@Override
	public String getDescription() {
		return "Renders static menu";
	}
	
	@Override
	public String getCommand() {
		return "/menu";
	}
	
	@Override
	public int getRequiredAccessLevel() {
		return 1;
	}
	
	@Override
	public void registerMenu(InlineContext ctx, InlineMenuBuilder builder) {
		builder
				.name("Main Menu")
				.button(new InlineButtonBuilder(ctx)
						.name("Button 1")
						.onQueryCallback(this::handleButtonClick)
						.row(0)
						.build())
				.button(new InlineButtonBuilder(ctx)
						.name("Button 2")
						.onQueryCallback(this::handleButtonClick)
						.row(0)
						.build())
				.button(new InlineButtonBuilder(ctx)
						.name("Button 3")
						.onQueryCallback(this::handleButtonClick)
						.row(0)
						.build())
				.button(new InlineButtonBuilder(ctx)
						.name("Sub menu")
						.row(0)
						.menu(new InlineMenuBuilder(ctx)
								.name("Sub menu")
								.button(new InlineButtonBuilder(ctx)
										.name("Sub Button 1")
										.onQueryCallback(this::handleButtonClick)
										.build())
								.button(new InlineButtonBuilder(ctx)
										.name("Sub Button 2")
										.onQueryCallback(this::handleButtonClick)
										.build())
								.button(new InlineButtonBuilder(ctx)
										.name("Sub Button 3")
										.onQueryCallback(this::handleButtonClick)
										.build())
								.button(defaultBack(ctx))
								.build())
						.build())
				.button(defaultClose(ctx));
	}
	
	private boolean handleButtonClick(InlineCallbackEvent event) throws TelegramApiException {
		final InlineUserData userData = event.getContext().getUserData(event.getQuery().getFrom().getId());
		final AnswerCallbackQuery answer = new AnswerCallbackQuery();
		answer.setCallbackQueryId(event.getQuery().getId());
		answer.setShowAlert(true);
		answer.setText("You've clicked at " + userData.getActiveButton().getName());
		event.getBot().execute(answer);
		return true;
	}
}
