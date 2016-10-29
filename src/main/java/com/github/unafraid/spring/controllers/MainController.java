package com.github.unafraid.spring.controllers;

import com.github.unafraid.spring.services.TelegramBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.objects.Update;

/**
 * Created by UnAfraid on 21.10.2016 г..
 */
@RestController
public class MainController {
    private TelegramBotService telegramBotService;

    @Autowired
    public MainController(TelegramBotService telegramBotService) {
        this.telegramBotService = telegramBotService;
    }

    @RequestMapping(value = "/bot", method = RequestMethod.POST)
    @ResponseBody
    public BotApiMethod onUpdateReceived(@RequestBody Update update) {
        return telegramBotService.onWebhookUpdateReceived(update);
    }
}
