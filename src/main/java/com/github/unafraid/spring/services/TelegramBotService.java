package com.github.unafraid.spring.services;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.ApiConstants;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.WebhookInfo;
import org.telegram.telegrambots.api.objects.inlinequery.ChosenInlineQuery;
import org.telegram.telegrambots.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import com.github.unafraid.spring.bot.TelegramWebhookBot;
import com.github.unafraid.spring.bot.handlers.general.CommandHandler;
import com.github.unafraid.spring.bot.handlers.general.IAccessLevelHandler;
import com.github.unafraid.spring.bot.handlers.general.ICallbackQueryHandler;
import com.github.unafraid.spring.bot.handlers.general.IChosenInlineQueryHandler;
import com.github.unafraid.spring.bot.handlers.general.ICommandHandler;
import com.github.unafraid.spring.bot.handlers.general.IEditedMessageHandler;
import com.github.unafraid.spring.bot.handlers.general.IInlineQueryHandler;
import com.github.unafraid.spring.bot.handlers.general.IMessageHandler;
import com.github.unafraid.spring.bot.util.BotUtil;
import com.github.unafraid.spring.bot.util.IThrowableFunction;
import com.github.unafraid.spring.config.TelegramBotConfig;

/**
 * @author UnAfraid
 */
@Service
public class TelegramBotService extends TelegramWebhookBot {
	private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotService.class);
	private static final Pattern COMMAND_ARGS_PATTERN = Pattern.compile("\"([^\"]*)\"|([^\\s]+)");

	@Inject
	private TelegramBotConfig config;

	@Inject
	private UsersService usersService;

	@Inject
	private void init(TelegramBotConfig config) throws Exception {
		final WebhookInfo info = getWebhookInfo();
		final String url = info.getUrl();
		final StringBuilder sb = new StringBuilder(config.getPath());
		if (sb.charAt(sb.length() - 1) != '/')
		{
			sb.append('/');
		}
		sb.append("callback/");
		sb.append(config.getToken());
		final String webHookUrl = sb.toString();
		LOGGER.info("Verifying web hook..");
		if (url == null || url.isEmpty() || !url.equals(webHookUrl)) {
			LOGGER.info("Web Hook URL require changes updating to: {} ..", webHookUrl);
			setWebhook(webHookUrl, "");
		} else {
			LOGGER.info("Web Hook is okay {}", webHookUrl);
		}
	}

	@Override
	public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
		if (update != null) {
			try {
				if (update.hasChosenInlineQuery()) {
					// Handle Chosen inline query
					handleUpdate(IChosenInlineQueryHandler.class, update, Update::getChosenInlineQuery, ChosenInlineQuery::getFrom, handler -> handler.onChosenInlineQuery(this, update, update.getChosenInlineQuery()));
				} else if (update.hasInlineQuery()) {
					// Handle inline query
					handleUpdate(IInlineQueryHandler.class, update, Update::getInlineQuery, InlineQuery::getFrom, handler -> handler.onInlineQuery(this, update, update.getInlineQuery()));
				} else if (update.hasCallbackQuery()) {
					// Handle callback query
					handleUpdate(ICallbackQueryHandler.class, update, Update::getCallbackQuery, CallbackQuery::getFrom, handler -> handler.onCallbackQuery(this, update, update.getCallbackQuery()));
				} else if (update.hasEditedMessage()) {
					// Handle edited message
					handleUpdate(IEditedMessageHandler.class, update, Update::getEditedMessage, Message::getFrom, handler -> handler.onEditMessage(this, update, update.getEditedMessage()));
				} else if (update.hasMessage()) {
					// Handle message
					handleIncomingMessage(update);
				} else {
					LOGGER.warn("Update doesn't contains neither ChosenInlineQuery/InlineQuery/CallbackQuery/EditedMessage/Message Update: {}", update);
				}
			} catch (Exception e) {
				LOGGER.error("Failed to handle incoming update", e);
			}
		}
		return null;
	}

	private <T, R> void handleUpdate(Class<T> clazz, Update update, Function<Update, R> dataMapper, Function<R, User> idMapper, IThrowableFunction<T, Boolean> action) {
		final R query = dataMapper.apply(update);
		if (query == null) {
			return;
		}

		final User user = idMapper.apply(query);
		final List<T> handlers = CommandHandler.getInstance().getHandlers(clazz, user.getId(), usersService);
		for (T handler : handlers) {
			try {
				if (action.apply(handler)) {
					break;
				}
			} catch (TelegramApiRequestException e) {
				LOGGER.warn("Exception caught on handler: {} error: {}", handler.getClass().getSimpleName(), e.getApiResponse(), e);
			} catch (Exception e) {
				LOGGER.warn("Exception caught on handler: {}", handler.getClass().getSimpleName(), e);
			}
		}
	}

	private void handleIncomingMessage(Update update) {
		final Message message = update.getMessage();
		if (message == null) {
			return;
		}

		String text = message.getText();
		if (text == null || text.isEmpty()) {
			return;
		}

		// Parse commands that goes like: @BotNickname help to /help
		if (text.startsWith("@" + getBotUsername() + " ")) {
			text = '/' + text.substring(("@" + getBotUsername() + " ").length());
		}
		// Parse commands that goes like: /help@BotNickname to /help
		else if (text.contains("@" + getBotUsername())) {
			text = text.replaceAll("@" + getBotUsername(), "");
			if (text.charAt(0) != '/') {
				text = '/' + text;
			}
		}

		// Parse arguments to a list
		final Matcher matcher = COMMAND_ARGS_PATTERN.matcher(text);
		if (matcher.find()) {
			String command = matcher.group();
			final List<String> args = new ArrayList<>();
			String arg;
			while (matcher.find()) {
				arg = matcher.group(1);
				if (arg == null) {
					arg = matcher.group(0);
				}

				args.add(arg);
			}

			final ICommandHandler handler = CommandHandler.getInstance().getHandler(command);
			if (handler != null) {
				try {
					if (!IAccessLevelHandler.validate(handler, message.getFrom().getId(), usersService)) {
						BotUtil.sendMessage(this, message, message.getFrom().getUserName() + ": You are not authorized to use this function!", true, false, null);
						return;
					}

					handler.onCommandMessage(this, update, message, args);
				} catch (TelegramApiRequestException e) {
					LOGGER.warn("API Exception caught on handler: {}, response: {} message: {}", handler.getClass().getSimpleName(), e.getApiResponse(), message, e);
				} catch (Exception e) {
					LOGGER.warn("Exception caught on handler: {}, message: {}", handler.getClass().getSimpleName(), message, e);
				}
			} else {
				for (IMessageHandler messageHandler : CommandHandler.getInstance().getHandlers(IMessageHandler.class, message.getFrom().getId(), usersService)) {
					try {
						if (messageHandler.onMessage(this, update, message)) {
							break;
						}
					} catch (TelegramApiRequestException e) {
						LOGGER.warn("API Exception caught on handler: {}, response: {} message: {}", messageHandler.getClass().getSimpleName(), e.getApiResponse(), message, e);
					} catch (Exception e) {
						LOGGER.warn("Exception caught on handler: {}, message: {}", messageHandler.getClass().getSimpleName(), message, e);
					}
				}
			}
		}
	}

	@Override
	public String getBotUsername() {
		return config.getUsername();
	}

	@Override
	public String getBotToken() {
		return config.getToken();
	}

	@Override
	public void setWebhook(String url, String publicCertificatePath) throws TelegramApiRequestException {
		try {
			final SetWebhook setWebhook = new SetWebhook();
			setWebhook.setUrl(url);
			if (publicCertificatePath != null && !publicCertificatePath.isEmpty()) {
				setWebhook.setCertificateFile(publicCertificatePath);
			}
			setWebhook.setMaxConnections(40);
			final String responseContent = BotUtil.doPostJSONQuery(this, SetWebhook.PATH, setWebhook);
			final JSONObject jsonObject = new JSONObject(responseContent);
			if (!jsonObject.getBoolean(ApiConstants.RESPONSE_FIELD_OK)) {
				throw new TelegramApiRequestException("Error setting web hook", jsonObject);
			}
			LOGGER.info("Web hook set: {}", jsonObject);
		} catch (JSONException e) {
			throw new TelegramApiRequestException("Error de-serializing setWebHook method response", e);
		} catch (Exception e) {
			throw new TelegramApiRequestException("Error executing setWebHook method", e);
		}
	}

	@Override
	public String getBotPath() {
		return config.getPath();
	}
}
