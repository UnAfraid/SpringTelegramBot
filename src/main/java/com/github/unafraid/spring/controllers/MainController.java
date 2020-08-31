package com.github.unafraid.spring.controllers;

import com.github.unafraid.spring.services.TelegramWebHookBotService;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author UnAfraid
 */
@RestController
public class MainController {
    private final TelegramWebHookBotService telegramBotService;

    public MainController(TelegramWebHookBotService telegramBotService) {
        this.telegramBotService = telegramBotService;
    }

    @RequestMapping(value = "/callback/${TELEGRAM_TOKEN}", method = RequestMethod.POST)
    @ResponseBody
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return telegramBotService.onWebhookUpdateReceived(update);
    }
}
