package com.github.unafraid.spring.bot.handlers.general.inline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import com.github.unafraid.spring.bot.handlers.general.IAccessLevelHandler;
import com.github.unafraid.spring.bot.handlers.general.ICallbackQueryHandler;
import com.github.unafraid.spring.bot.handlers.general.ICancelHandler;
import com.github.unafraid.spring.bot.handlers.general.ICommandHandler;
import com.github.unafraid.spring.bot.handlers.general.IMessageHandler;
import com.github.unafraid.spring.bot.util.BotUtil;

/**
 * Created by UnAfraid on 30.10.2016 г..
 */
public abstract class AbstractInlineMenu<T extends AbstractCommandData> implements ICommandHandler, IAccessLevelHandler, IMessageHandler, ICallbackQueryHandler, ICancelHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInlineMenu.class);
	private final List<IInlineCommandType> _commands = new ArrayList<>();
	private final Map<Integer, T> _data = new ConcurrentHashMap<>();

	protected void addCommand(IInlineCommandType type) {
		_commands.add(type);
	}

	@Override
	public boolean onMessage(AbsSender bot, Update update, Message message) throws TelegramApiException {
		final T data = _data.computeIfAbsent(message.getFrom().getId(), this::createData);
		return onMessageInput(bot, message, data);
	}


	@Override
	public final void onCommandMessage(AbsSender bot, Update update, Message message, List<String> args) throws TelegramApiException {
		final T data = _data.computeIfAbsent(message.getFrom().getId(), this::createData);
		if (data.getState() == 0) {
			final InlineKeyboardMarkup markup = generateMenu(_commands);
			if (!markup.getKeyboard().isEmpty()) {
				BotUtil.sendMessage(bot, message, "Commands: ", true, false, markup);
			}
		}
	}

	@Override
	public boolean onCallbackQuery(AbsSender bot, Update update, CallbackQuery query) throws TelegramApiException {
		final T data = _data.computeIfAbsent(query.getFrom().getId(), this::createData);
		final IInlineCommandType type = getCommandType(query.getData(), _commands);
		if (type != null) {
			data.setType(type);
			if (!type.getSubCommands().isEmpty()) {
				final InlineKeyboardMarkup markup = generateMenu(type.getSubCommands());
				if (!markup.getKeyboard().isEmpty()) {
					BotUtil.editMessage(bot, query.getMessage(), "Sub Commands: ", false, markup);
				}
			}
		}
		return onMenuSelect(bot, update, query, data);
	}


	@Override
	public void onCancel(AbsSender bot, Update update, Message message) throws TelegramApiException {
		_data.clear();
	}

	private IInlineCommandType getCommandType(String text, List<IInlineCommandType> commands) {
		for (IInlineCommandType command : commands) {
			if (text.equalsIgnoreCase(command.getName())) {
				return command;
			}

			final IInlineCommandType cmd = getCommandType(text, command.getSubCommands());
			if (cmd != null) {
				return cmd;
			}
		}
		return null;
	}

	protected void back(AbsSender bot, Message message, T data) throws TelegramApiException {
		data.clear();
		final InlineKeyboardMarkup markup = generateMenu(_commands);
		BotUtil.editMessage(bot, message, "Commands: ", false, markup);
	}

	protected abstract boolean onMenuSelect(AbsSender bot, Update update, CallbackQuery query, T data) throws TelegramApiException;

	protected abstract boolean onMessageInput(AbsSender bot, Message message, T data) throws TelegramApiException;

	protected abstract T createData(int ownerId);

	protected InlineKeyboardMarkup generateMenu(List<IInlineCommandType> commands) throws TelegramApiException {
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
