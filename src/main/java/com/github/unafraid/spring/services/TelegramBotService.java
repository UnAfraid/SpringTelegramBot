package com.github.unafraid.spring.services;

import com.github.unafraid.spring.bot.AccessLevelValidator;
import com.github.unafraid.spring.bot.TelegramWebHookBot;
import com.github.unafraid.spring.config.TelegramBotConfig;
import com.github.unafraid.telegrambot.handlers.ICommandHandler;
import com.github.unafraid.telegrambot.handlers.ITelegramHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.updates.GetWebhookInfo;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.WebhookInfo;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author UnAfraid
 */
@Service
public class TelegramBotService {
    private final TelegramBotConfig config;
    private final ApplicationContext appContext;
    private final AccessLevelValidator accessLevelValidator;
    private final ObjectProvider<TelegramClient> telegramClientProvider;

    // Compose a TelegramWebHookBot instance and initialize it after context startup
    private TelegramWebHookBot bot;

    public TelegramBotService(TelegramBotConfig config,
                              ApplicationContext appContext,
                              AccessLevelValidator accessLevelValidator,
                              @NotNull ObjectProvider<TelegramClient> telegramClientProvider
    ) {
        this.config = config;
        this.appContext = appContext;
        this.accessLevelValidator = accessLevelValidator;
        this.telegramClientProvider = telegramClientProvider;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() throws Exception {
        // instantiate the underlying bot now (after context is ready)
        this.bot = new TelegramWebHookBot(config.getToken(), appContext, telegramClientProvider, accessLevelValidator);

        // Register handlers after the context is fully initialized
        final var handlerBeans = appContext.getBeansOfType(ICommandHandler.class).values();
        bot.registerHandlers(handlerBeans);

        // Perform webhook initialization that might call external APIs
        initializeWebhookIfNeeded();
    }

    private void initializeWebhookIfNeeded() throws Exception {
        final WebhookInfo info = bot.execute(new GetWebhookInfo());
        final String url = info.getUrl();
        final String webHookUrl = computeCallbackEndpoint();

        if (url == null || url.isEmpty() || !url.equals(webHookUrl) || info.getMaxConnections() != config.getMaxConnections()) {
            bot.execute(SetWebhook.builder().
                    url(webHookUrl).
                    maxConnections(config.getMaxConnections()).
                    build());
        }


        registerMyCommands();
    }

    private String computeCallbackEndpoint() {
        final StringBuilder sb = new StringBuilder(config.getUrl());
        if (sb.charAt(sb.length() - 1) != '/') {
            sb.append('/');
        }
        sb.append("callback/");
        sb.append(config.getToken());
        return sb.toString();
    }

    private void registerMyCommands() throws TelegramApiException {
        final List<BotCommand> botCommandList = new ArrayList<>();
        for (ITelegramHandler handler : bot.getHandlers()) {
            if (handler instanceof ICommandHandler commandHandler) {
                botCommandList.add(new BotCommand(commandHandler.getCommand(), commandHandler.getDescription()));
            }
        }

        if (!botCommandList.isEmpty()) {
            bot.execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), config.getLanguageCode()));
        }
    }

    // Expose the webhook update entry point used by controllers
    public void onWebhookUpdateReceived(Update update) {
        if (bot != null) {
            bot.onWebhookUpdateReceived(update);
        }
    }
}
