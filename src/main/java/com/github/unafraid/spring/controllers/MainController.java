package com.github.unafraid.spring.controllers;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.objects.Update;
import com.github.unafraid.spring.services.TelegramWebHookBotService;

/**
 * @author UnAfraid
 */
@RestController
public class MainController {
	@Inject
	private TelegramWebHookBotService telegramBotService;

	@RequestMapping(value = "/callback/${TELEGRAM_TOKEN}", method = RequestMethod.POST)
	@ResponseBody
	public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
		return telegramBotService.onWebhookUpdateReceived(update);
	}
}
