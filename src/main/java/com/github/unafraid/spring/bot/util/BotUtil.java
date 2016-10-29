package com.github.unafraid.spring.bot.util;

import com.github.unafraid.spring.bot.handlers.impl.ICommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author UnAfraid
 */
public class BotUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(BotUtil.class);

    public static <T> T initialize(Supplier<T> providerOne, Supplier<T> providerTwo) {
        try {
            final T one = providerOne.get();
            return one;
        } catch (Exception e) {
            LOGGER.warn("Failed to initialize first class: {} attempting to initialize second: {}", providerOne, providerTwo);
            try {
                final T two = providerTwo.get();
                return two;
            } catch (Exception e2) {
                LOGGER.warn("Failed to initialize second class: {}", providerTwo);
            }
        }
        return null;
    }

    public static int parseInt(String intValue, int defaultValue) {
        try {
            return Integer.parseInt(intValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

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

    public static boolean sendIcon(String fileName, AbsSender bot, Message message, boolean useCaption) throws IOException, TelegramApiException {
        sendAction(bot, message, ActionType.UPLOADPHOTO);

        try (ZipInputStream zipInputStream = new ZipInputStream(BotUtil.class.getResourceAsStream("/images.zip"))) {
            final String iconName = fileName + ".png";
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().equalsIgnoreCase(iconName)) {
                    if (useCaption) {
                        sendPhoto(bot, message, iconName, iconName, zipInputStream);
                    } else {
                        sendPhoto(bot, message, null, iconName, zipInputStream);
                    }
                    return true;
                }
            }
        }
        return false;
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
        final DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    /**
     * @param string
     * @return
     */
    public static boolean isDigit(String string) {
        try {
            Integer.parseInt(string);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static int getPID() {
        try {
            final RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            final Field jvmField = runtime.getClass().getDeclaredField("jvm");
            jvmField.setAccessible(true);

            final Object mgmt = jvmField.get(runtime);
            final Method pidMethod = mgmt.getClass().getDeclaredMethod("getProcessId");
            pidMethod.setAccessible(true);

            return (Integer) pidMethod.invoke(mgmt);
        } catch (Exception e) {
            LOGGER.warn("Couldn't find PID", e);
        }
        return -1;
    }

    public static final void writePID(File pid) {
        try {
            Files.write(pid.toPath(), String.valueOf(BotUtil.getPID()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (Exception e) {
            LOGGER.warn("Couldn't write PID", e);
        }
    }
}
