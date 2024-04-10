package com.github.unafraid.spring.services;

import com.github.unafraid.spring.bot.AccessLevelValidator;
import com.github.unafraid.spring.bot.TelegramWebHookBot;
import com.github.unafraid.spring.config.TelegramBotConfig;
import com.github.unafraid.telegrambot.handlers.ICommandHandler;
import com.github.unafraid.telegrambot.handlers.ITelegramHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.updates.GetWebhookInfo;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.ApiResponse;
import org.telegram.telegrambots.meta.api.objects.WebhookInfo;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author UnAfraid
 */
@Service
public class TelegramBotService extends TelegramWebHookBot {
	private final TelegramBotConfig config;
	
	public TelegramBotService(TelegramBotConfig config,
							  ApplicationContext appContext,
							  AccessLevelValidator accessLevelValidator,
							  @NotNull ObjectProvider<TelegramClient> telegramClientProvider
							  ) throws Exception {
		super(config.getToken(), appContext, telegramClientProvider, accessLevelValidator);
		this.config = config;
		init();
	}
	
	private void init() throws Exception {
		final WebhookInfo info = execute(new GetWebhookInfo());
		final String url = info.getUrl();
		final String webHookUrl = computeCallbackEndpoint();
		
		if (url == null || url.isEmpty() || !url.equals(webHookUrl) || info.getMaxConnections() != config.getMaxConnections()) {
			setWebhook(SetWebhook.builder().
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
		for (ITelegramHandler handler : getHandlers()) {
			if (handler instanceof ICommandHandler commandHandler) {
                botCommandList.add(new BotCommand(commandHandler.getCommand(), commandHandler.getDescription()));
			}
		}
		
		if (!botCommandList.isEmpty()) {
			execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), config.getLanguageCode()));
		}
	}

	private void setWebhook(SetWebhook setWebhook) throws TelegramApiException {
		try {
			final RestTemplate rest = new RestTemplate();
			final HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", "application/json");
			headers.add("Accept", "application/json");
			
			final String setWebhookUrl = String.format("https://api.telegram.org/bot%s/%s", config.getToken(), SetWebhook.PATH);
			rest.exchange(setWebhookUrl, HttpMethod.POST, new HttpEntity<>(setWebhook, headers), ApiResponse.class);
		} catch (Exception e) {
			throw new TelegramApiRequestException("Error executing setWebHook method", e);
		}
	}
}
