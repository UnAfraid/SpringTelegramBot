package com.github.unafraid.spring.bot;

import com.github.unafraid.telegrambot.bots.DefaultTelegramBot;
import com.github.unafraid.telegrambot.handlers.ICommandHandler;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.WebhookBot;

import java.util.Map;
import java.util.Optional;

/**
 * @author UnAfraid
 */
public abstract class TelegramWebHookBot extends DefaultTelegramBot implements WebhookBot {
    public TelegramWebHookBot(String token, String username, ApplicationContext appContext, Optional<DefaultBotOptions> defaultBotOptions, AccessLevelValidator accessLevelValidator) {
        super(token, username, defaultBotOptions.orElse(new DefaultBotOptions()));

        setAccessLevelValidator(accessLevelValidator);

        final Map<String, ICommandHandler> handlers = appContext.getBeansOfType(ICommandHandler.class);
        handlers.values().forEach(this::addHandler);
    }

    @Override
    public final BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update != null) {
            onUpdateReceived(update);
        }
        return null;
    }
}
