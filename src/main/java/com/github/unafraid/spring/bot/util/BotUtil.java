package com.github.unafraid.spring.bot.util;

import com.github.unafraid.spring.bot.handlers.impl.ICommandHandler;
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

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author UnAfraid
 */
public class BotUtil {
    public static void sendAction(AbsSender bot, Message message, ActionType actionType) throws TelegramApiException {
        final SendChatAction sendAction = new SendChatAction();
        sendAction.setChatId(Long.toString(message.getChat().getId()));
        sendAction.setAction(actionType);
        bot.sendChatAction(sendAction);
    }

    public static void sendUsage(AbsSender bot, Message message, ICommandHandler handler) throws TelegramApiException {
        final SendMessage msg = new SendMessage();
        msg.setChatId(Long.toString(message.getChat().getId()));
        msg.setText(handler.getUsage());
        bot.sendMessage(msg);
    }

    public static void sendMessage(AbsSender bot, Message message, String text, boolean replyToMessage, boolean useMarkDown, ReplyKeyboard replayMarkup) throws TelegramApiException {
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
        bot.sendMessage(msg);
    }

    public static void editMessage(AbsSender bot, Message message, String text, boolean useMarkDown, InlineKeyboardMarkup inlineMarkup) throws TelegramApiException {
        final EditMessageText msg = new EditMessageText();
        msg.setChatId(Long.toString(message.getChat().getId()));
        msg.setMessageId(message.getMessageId());
        msg.setText(text);
        msg.enableMarkdown(useMarkDown);
        msg.setReplyMarkup(inlineMarkup);
        bot.editMessageText(msg);
    }

    public static void editMessage(AbsSender bot, CallbackQuery query, String text, boolean useMarkDown, InlineKeyboardMarkup inlineMarkup) throws TelegramApiException {
        final EditMessageText msg = new EditMessageText();
        msg.setChatId(Long.toString(query.getMessage().getChat().getId()));
        msg.setMessageId(query.getMessage().getMessageId());
        msg.setInlineMessageId(query.getInlineMessageId());
        msg.setText(text);
        msg.enableMarkdown(useMarkDown);
        msg.setReplyMarkup(inlineMarkup);
        bot.editMessageText(msg);
    }

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

    public static InlineKeyboardButton createButton(String text) {
        return new InlineKeyboardButton().setText(text).setCallbackData(text);
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
     * @param string
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
     * @param intValue
     * @param defaultValue
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
