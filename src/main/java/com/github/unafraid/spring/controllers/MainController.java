package com.github.unafraid.spring.controllers;

import com.github.unafraid.spring.services.TelegramBotService;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.objects.Update;

import javax.inject.Inject;

/**
 * @author UnAfraid
 */
@RestController
public class MainController {
    @Inject
    private TelegramBotService telegramBotService;

    @RequestMapping(value = "/${TELEGRAM_TOKEN}", method = RequestMethod.POST)
    @ResponseBody
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return telegramBotService.onWebhookUpdateReceived(update);
    }
}
