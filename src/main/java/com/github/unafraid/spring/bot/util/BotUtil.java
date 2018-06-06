package com.github.unafraid.spring.bot.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.telegram.telegrambots.ApiConstants;
import org.telegram.telegrambots.api.methods.ActionType;
import org.telegram.telegrambots.api.methods.send.SendChatAction;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.generics.WebhookBot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.unafraid.telegrambot.handlers.ICommandHandler;

/**
 * @author UnAfraid
 */
public class BotUtil {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	public static final Map<String, String> STANDARD_HEADERS;

	static {
		final Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		STANDARD_HEADERS = Collections.unmodifiableMap(headers);
	}

	/**
	 * Creates POST request with json as body.
	 *
	 * @param bot    the bot
	 * @param url    the url
	 * @param object the object to write
	 * @return the response
	 * @throws TelegramApiException the exception
	 */
	public static String doPostJSONQuery(WebhookBot bot, String url, Object object) throws TelegramApiException {
		return doPostJSONQuery(bot, url, STANDARD_HEADERS, object);
	}

	/**
	 * Creates POST request with json as body.
	 *
	 * @param bot     the bot
	 * @param url     the url
	 * @param headers the headers map
	 * @param object  the object to write
	 * @return the response
	 * @throws TelegramApiException the exception
	 */
	public static String doPostJSONQuery(WebhookBot bot, String url, Map<String, String> headers, Object object) throws TelegramApiException {
		try {
			Objects.requireNonNull(bot, "bot cannot be null!");
			Objects.requireNonNull(url, "url cannot be null!");
			Objects.requireNonNull(headers, "headers cannot be null!");
			Objects.requireNonNull(object, "object cannot be null!");

			final StringBuilder sb = new StringBuilder(ApiConstants.BASE_URL);
			sb.append(bot.getBotToken());
			sb.append('/');
			sb.append(url);
			final URL urlAddress = new URL(sb.toString());
			final HttpURLConnection connection = (HttpURLConnection) urlAddress.openConnection();

			// Set POST type
			connection.setRequestMethod("POST");

			// Set headers
			headers.entrySet().forEach(entry -> connection.setRequestProperty(entry.getKey(), entry.getValue()));

			// Set output
			connection.setDoOutput(true);

			// Write output
			try (final OutputStream os = connection.getOutputStream();
				 final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8.name()))) {
				writer.write(OBJECT_MAPPER.writeValueAsString(object));
			}

			connection.connect();

			// Read input
			try (final InputStreamReader input = new InputStreamReader(connection.getInputStream());
				 final BufferedReader reader = new BufferedReader(input)) {
				return reader.lines().parallel().collect(Collectors.joining(System.lineSeparator()));
			} finally {
				connection.disconnect();
			}
		} catch (Exception e) {
			throw new TelegramApiException("Unable to execute post query", e);
		}
	}

	/**
	 * Sends action
	 *
	 * @param bot        the bot
	 * @param message    the message
	 * @param actionType the type
	 * @throws TelegramApiException the exception
	 */
	public static void sendAction(AbsSender bot, Message message, ActionType actionType) throws TelegramApiException {
		final SendChatAction sendAction = new SendChatAction();
		sendAction.setChatId(Long.toString(message.getChat().getId()));
		sendAction.setAction(actionType);
		bot.execute(sendAction);
	}

	/**
	 * Sends the usage as message
	 *
	 * @param bot     the bot
	 * @param message the message
	 * @param handler the handler
	 * @throws TelegramApiException the exception
	 */
	public static void sendUsage(AbsSender bot, Message message, ICommandHandler handler) throws TelegramApiException {
		final SendMessage msg = new SendMessage();
		msg.setChatId(Long.toString(message.getChat().getId()));
		msg.setText(handler.getUsage());
		bot.execute(msg);
	}

	/**
	 * Sends a message,
	 *
	 * @param bot            the bot
	 * @param message        them essage
	 * @param text           the text (CANNOT BE EMPTY)
	 * @param replyToMessage reply to the message
	 * @param useMarkDown    use markdown like *bold text here*
	 * @param replayMarkup   markup like keyboards
	 * @throws TelegramApiException the exception
	 */
	public static void sendMessage(AbsSender bot, Message message, String text, boolean replyToMessage, boolean useMarkDown, ReplyKeyboard replayMarkup) throws TelegramApiException {
		if (text == null || text.isEmpty()) {
			throw new TelegramApiException("Message cannot  be null or empty!");
		}
		final SendMessage msg = new SendMessage();
		msg.setChatId(Long.toString(message.getChat().getId()));
		msg.setText(text);
		msg.enableMarkdown(useMarkDown);
		if (replyToMessage) {
			msg.setReplyToMessageId(message.getMessageId());
		}
		if (replayMarkup != null) {
			msg.setReplyMarkup(replayMarkup);
		}
		bot.execute(msg);
	}

	/**
	 * Edits a message, Keep in mind it will throw exception if u keep same text or put empty as text
	 *
	 * @param bot          the bot
	 * @param message      the message
	 * @param text         the text (CANNOT BE EMPTY)
	 * @param useMarkDown  use markdown like *bold text here*
	 * @param inlineMarkup inline keyboard markup
	 * @throws TelegramApiException the exception here
	 */
	public static void editMessage(AbsSender bot, Message message, String text, boolean useMarkDown, InlineKeyboardMarkup inlineMarkup) throws TelegramApiException {
		final EditMessageText msg = new EditMessageText();
		msg.setChatId(Long.toString(message.getChat().getId()));
		msg.setMessageId(message.getMessageId());
		msg.setText(text);
		msg.enableMarkdown(useMarkDown);
		msg.setReplyMarkup(inlineMarkup);
		bot.execute(msg);
	}

	/**
	 * Edits a message, Keep in mind it will throw exception if u keep same text or put empty as text
	 *
	 * @param bot          the bot
	 * @param query        the query
	 * @param text         the text (CANNOT BE EMPTY OR SAME AS PREVIOUS MESSAGE)
	 * @param useMarkDown  use markdown like *bold text here*
	 * @param inlineMarkup markup like keyboards
	 * @throws TelegramApiException the exception
	 */
	public static void editMessage(AbsSender bot, CallbackQuery query, String text, boolean useMarkDown, InlineKeyboardMarkup inlineMarkup) throws TelegramApiException {
		final EditMessageText msg = new EditMessageText();
		msg.setChatId(Long.toString(query.getMessage().getChat().getId()));
		msg.setMessageId(query.getMessage().getMessageId());
		msg.setInlineMessageId(query.getInlineMessageId());
		msg.setText(text);
		msg.enableMarkdown(useMarkDown);
		msg.setReplyMarkup(inlineMarkup);
		bot.execute(msg);
	}

	/**
	 * Sends a photo using InputStream
	 *
	 * @param bot        the bot
	 * @param message    the message
	 * @param caption    the caption
	 * @param fileName   the filename
	 * @param dataStream the data input stream
	 * @throws TelegramApiException the exception
	 */
	public static void sendPhoto(AbsSender bot, Message message, String caption, String fileName, InputStream dataStream) throws TelegramApiException {
		final SendPhoto photo = new SendPhoto();
		photo.setChatId(Long.toString(message.getChat().getId()));
		photo.setPhoto(fileName);
		photo.setNewPhoto(fileName, dataStream);
		if (caption != null) {
			photo.setCaption(caption);
		}
		photo.setReplyToMessageId(message.getMessageId());
		bot.sendPhoto(photo);
	}

	/**
	 * Creates a button with text and callback same as text
	 *
	 * @param text the text
	 * @return the button
	 */
	public static InlineKeyboardButton createButton(String text) {
		return new InlineKeyboardButton().setText(text).setCallbackData(text);
	}


	/**
	 * Creates a button with text an callback given
	 *
	 * @param text     the text
	 * @param callback the callback
	 * @return the button
	 */
	public static InlineKeyboardButton createButton(String text, String callback) {
		return new InlineKeyboardButton().setText(text).setCallbackData(callback);
	}

	/**
	 * Format the given date on the given format
	 *
	 * @param date   : the date to format.
	 * @param format : the format to correct by.
	 * @return a string representation of the formatted date.
	 */
	public static String formatDate(Date date, String format) {
		if (date == null) {
			return null;
		}
		return new SimpleDateFormat(format).format(date);
	}

	/**
	 * @param string the string
	 * @return {@code true} if the text is integer, {@code false} otherwise
	 */
	public static boolean isDigit(String string) {
		try {
			Integer.parseInt(string);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * @param intValue     the int value
	 * @param defaultValue the default value
	 * @return the int from the given string or defaultValue in case its not an int
	 */
	public static int parseInt(String intValue, int defaultValue) {
		try {
			return Integer.parseInt(intValue);
		} catch (Exception e) {
			return defaultValue;
		}
	}
}
