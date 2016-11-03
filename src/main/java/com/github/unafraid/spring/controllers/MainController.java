package com.github.unafraid.spring.controllers;

import com.github.unafraid.spring.services.TelegramBotService;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.objects.Update;

import javax.inject.Inject;

/**
 * Created by UnAfraid on 21.10.2016 г..
 */
@RestController
public class MainController {
    @Inject
    private TelegramBotService telegramBotService;

    @RequestMapping(value = "/bot", method = RequestMethod.POST)
    @ResponseBody
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return telegramBotService.onWebhookUpdateReceived(update);
    }
}
