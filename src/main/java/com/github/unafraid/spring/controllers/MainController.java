package com.github.unafraid.spring.controllers;

import com.github.unafraid.spring.services.TelegramBotService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author UnAfraid
 */
@RestController
public class MainController {
	private final TelegramBotService telegramBotService;
	
	public MainController(TelegramBotService telegramBotService) {
		this.telegramBotService = telegramBotService;
	}
	
	@RequestMapping(value = "/callback/${TELEGRAM_TOKEN}", method = RequestMethod.POST)
	@ResponseBody
	public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
		return telegramBotService.onWebhookUpdateReceived(update);
	}
}
