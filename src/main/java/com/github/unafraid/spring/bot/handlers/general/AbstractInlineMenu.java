package com.github.unafraid.spring.bot.handlers.general;

import com.github.unafraid.spring.bot.handlers.impl.ICommandHandler;
import com.github.unafraid.spring.bot.util.BotUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by UnAfraid on 30.10.2016 г..
 */
public abstract class AbstractInlineMenu<T extends AbstractCommandData> implements ICommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInlineMenu.class);
    private List<ICommandType> _commands = new ArrayList<>();
    private final Map<Integer, T> _datas = new ConcurrentHashMap<>();

    protected void addCommand(ICommandType type) {
        _commands.add(type);
    }

    @Override
    public boolean onMessage(AbsSender bot, Message message, List<String> args) throws TelegramApiException {
        final T data = _datas.computeIfAbsent(message.getFrom().getId(), this::createData);
        return onMessageInput(bot, message, data);
    }


    @Override
    public final void onMessage(AbsSender bot, Message message, int updateId, List<String> args) throws TelegramApiException {
        final T data = _datas.computeIfAbsent(message.getFrom().getId(), this::createData);
        if (data.getState() == 0) {
            final InlineKeyboardMarkup markup = generateMenu(_commands);
            if (!markup.getKeyboard().isEmpty()) {
                BotUtil.sendMessage(bot, message, "Commands: ", true, false, markup);
            }
        }
    }

    @Override
    public boolean onCallback(AbsSender bot, Update update, CallbackQuery query) throws TelegramApiException {
        final T data = _datas.computeIfAbsent(query.getFrom().getId(), this::createData);
        final ICommandType type = getCommandType(query.getData(), _commands);
        if (type != null) {
            data.setType(type);
            if (!type.getSubCommands().isEmpty()) {
                final InlineKeyboardMarkup markup = generateMenu(type.getSubCommands());
                if (!markup.getKeyboard().isEmpty()) {
                    BotUtil.editMessage(bot, query.getMessage(), "Sub Commands: ", false, markup);
                }
            }
            return onMenuSelect(bot, update, query, data);
        }
        return onMenuSelect(bot, update, query, data);
    }

    private ICommandType getCommandType(String text, List<ICommandType> commands) {
        for (ICommandType command : commands) {
            if (text.equalsIgnoreCase(command.getName())) {
                return command;
            }

            final ICommandType cmd = getCommandType(text, command.getSubCommands());
            if (cmd != null) {
                return cmd;
            }
        }
        return null;
    }

    protected void back(AbsSender bot, Message message, T data) throws TelegramApiException {
        data.clear();
        final InlineKeyboardMarkup markup = generateMenu(getCommands());
        BotUtil.editMessage(bot, message, "Commands: ", false, markup);
    }

    protected List<ICommandType> getCommands() {
        return _commands;
    }

    protected abstract boolean onMenuSelect(AbsSender bot, Update update, CallbackQuery query, T data) throws TelegramApiException;

    protected abstract boolean onMessageInput(AbsSender bot, Message message, T data) throws TelegramApiException;

    protected abstract T createData(int ownerId);

    protected InlineKeyboardMarkup generateMenu(List<ICommandType> commands) throws TelegramApiException {
        final InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        commands.forEach(command ->
        {
            final InlineKeyboardButton button = BotUtil.createButton(command.getName());
            final List<InlineKeyboardButton> row;
            if (markup.getKeyboard().size() <= command.getRow()) {
                markup.getKeyboard().add(new ArrayList<>());
            }
            row = markup.getKeyboard().get(command.getRow());
            row.add(button);
        });
        LOGGER.debug("Commands: {}", markup.getKeyboard());
        return markup;
    }
}
