package com.github.unafraid.spring.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.unafraid.spring.services.TelegramBotService;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author UnAfraid
 */
@RestController
public class MainController {
	private final TelegramBotService telegramBotService;
	private final ObjectMapper objectMapper;

	public MainController(@Lazy TelegramBotService telegramBotService, ObjectMapper objectMapper) {
		this.telegramBotService = telegramBotService;
		this.objectMapper = objectMapper;
	}

	@PostMapping(
			value = "/callback/${TELEGRAM_TOKEN}",
			produces = "application/json",
			consumes = "application/json"
	)
	public void onUpdateReceived(@RequestBody String body) throws JsonProcessingException {
		Update update = objectMapper.readValue(body, Update.class);
		telegramBotService.onWebhookUpdateReceived(update);
	}
}
