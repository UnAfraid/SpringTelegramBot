package com.github.unafraid.spring.bot.handlers.impl;

import com.github.unafraid.spring.bot.db.model.User;
import com.github.unafraid.spring.bot.db.services.IUsersService;
import com.github.unafraid.spring.bot.handlers.general.AbstractCommandData;
import com.github.unafraid.spring.bot.handlers.general.AbstractInlineMenu;
import com.github.unafraid.spring.bot.handlers.general.ICommandType;
import com.github.unafraid.spring.bot.util.BotUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by UnAfraid on 30.10.2016 г..
 */
@Service
public class UsersHandler extends AbstractInlineMenu<UsersHandler.UserData> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsersHandler.class);

    @Inject
    private IUsersService usersService;

    @Override
    public String getCommand() {
        return "/users";
    }

    @Override
    public String getUsage() {
        return "/users";
    }

    @Override
    public String getDescription() {
        return "Manages users";
    }

    @Override
    public int getRequiredAccessLevel() {
        return 7;
    }

    protected UsersHandler() {
        // Add Commands
        Arrays.stream(Commands.values()).forEach(this::addCommand);
    }

    @Override
    protected boolean onMenuSelect(AbsSender bot, Update update, CallbackQuery query, UserData data) throws TelegramApiException {
        final ICommandType type = data.getType();
        if (type == null) {
            return false;
        }

        if (type == Commands.ADD) {
            BotUtil.editMessage(bot, query.getMessage(), "Tell me that user's id", false, null);
            data.setState(1);
            return true;
        } else if (type == Commands.EDIT) {
            switch (data.getState()) {
                case 0: {
                    sendUserButtons(bot, query, data);
                    data.setState(1);
                    return true;
                }
                case 1: {
                    final User userToEdit = usersService.findAll().stream().filter(user -> user.getName().equalsIgnoreCase(query.getData())).findFirst().orElse(null);
                    if (userToEdit == null) {
                        return false;
                    }
                    data.setUser(userToEdit);
                    final InlineKeyboardMarkup markup = generateMenu(Arrays.stream(EditCommands.values()).collect(Collectors.toList()));
                    BotUtil.editMessage(bot, query.getMessage(), "Select what would u edit for " + userToEdit.getName(), false, markup);
                    data.setState(2);
                    return true;
                }
            }
        } else if (type == Commands.DELETE) {
            switch (data.getState()) {
                case 0: {
                    sendUserButtons(bot, query, data);
                    data.setState(1);
                    return true;
                }
                case 1: {
                    final String username = query.getData();
                    if ("Back".equalsIgnoreCase(username)) {
                        back(bot, query.getMessage(), data);
                        return true;
                    }

                    final User userToDelete = usersService.findAll().stream().filter(user -> user.getName().equalsIgnoreCase(username)).findFirst().orElse(null);
                    if (userToDelete != null) {
                        if (usersService.delete(userToDelete.getId()) != null) {
                            BotUtil.editMessage(bot, query.getMessage(), "User " + username + " has been deleted", false, null);
                        } else {
                            BotUtil.editMessage(bot, query.getMessage(), "Failed to delete " + username, false, null);
                        }
                        data.clear();
                        return true;
                    }
                    return false;
                }
            }
        } else if (type == Commands.LIST) {
            switch (data.getState()) {
                case 0: {
                    sendUserButtons(bot, query, data);
                    data.setState(1);
                    return true;
                }
                case 1: {
                    if ("Back".equalsIgnoreCase(query.getData())) {
                        back(bot, query.getMessage(), data);
                        return true;
                    }
                    final User user = usersService.findAll().stream().filter(u -> u.getName().equalsIgnoreCase(query.getData())).findFirst().orElse(null);
                    final AnswerCallbackQuery answer = new AnswerCallbackQuery();
                    answer.setText(user != null ? "User: [" + user.getId() + "](" + user.getName() + ") Level: " + user.getLevel() : "U've clicked at " + query.getData());
                    answer.setCallbackQueryId(query.getId());
                    answer.setShowAlert(true);
                    bot.answerCallbackQuery(answer);
                    return false;
                }
            }
        } else if (type == Commands.DONE) {
            BotUtil.editMessage(bot, query.getMessage(), "Type /users again if u wanna do some changes", false, null);
            return true;
        }

        if (query.getData().equalsIgnoreCase(EditCommands.USERNAME.getName())) {
            switch (data.getState()) {
                case 2: {
                    BotUtil.editMessage(bot, query.getMessage(), "What is the new username?", false, null);
                    data.setType(EditCommands.USERNAME);
                    data.setState(3);
                    break;
                }
            }
            return true;
        } else if (query.getData().equalsIgnoreCase(EditCommands.LEVEL.getName())) {
            switch (data.getState()) {
                case 2: {
                    BotUtil.editMessage(bot, query.getMessage(), "What is the new level?", false, null);
                    data.setType(EditCommands.LEVEL);
                    data.setState(3);
                    break;
                }
            }
            return true;
        } else if (query.getData().equalsIgnoreCase(EditCommands.BACK.getName())) {
            back(bot, query.getMessage(), data);
            return true;
        } else {
            final AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setText(type.getClass().getSimpleName() + " (" + query.getData() + ") is not implemented");
            answer.setCallbackQueryId(query.getId());
            bot.answerCallbackQuery(answer);
        }

        final AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setText(type.getClass().getSimpleName() + " [" + query.getData() + "] is not implemented");
        answer.setCallbackQueryId(query.getId());
        bot.answerCallbackQuery(answer);
        return false;
    }

    private void sendUserButtons(AbsSender bot, CallbackQuery query, UserData data) throws TelegramApiException {
        final InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        final List<List<InlineKeyboardButton>> buttons = markup.getKeyboard();
        usersService.findAll().forEach(user ->
        {
            if (buttons.isEmpty() || (buttons.get(buttons.size() - 1).size() >= 3)) {
                buttons.add(new ArrayList<>());
            }
            buttons.get(buttons.size() - 1).add(BotUtil.createButton(user.getName()));
        });
        markup.getKeyboard().add(Arrays.asList(BotUtil.createButton("Back")));
        BotUtil.editMessage(bot, query.getMessage(), "Tell me that user's id", false, markup);
    }

    @Override
    protected boolean onMessageInput(AbsSender bot, Message message, UserData data) throws TelegramApiException {
        final ICommandType type = data.getType();
        if (type == Commands.ADD) {
            switch (data.getState()) {
                case 1: {
                    final String text = message.getText();
                    final int id = parseInt(text, 0);
                    if (id > 0) {
                        data.setId(id);
                        data.setState(2);
                        BotUtil.sendMessage(bot, message, "Great, now give me the user name", false, false, null);
                        return true;
                    }
                    break;
                }
                case 2: {
                    final String text = message.getText();
                    if (text == null || text.isEmpty()) {
                        return true;
                    }
                    data.setName(text);
                    BotUtil.sendMessage(bot, message, "What access level shall i set to " + data.getName() + " ?", false, false, null);
                    data.setState(3);
                    return true;
                }
                case 3: {
                    final String text = message.getText();
                    final int level = parseInt(text, 0);
                    if (level > 0) {
                        final User user = usersService.create(data.getId(), data.getName(), level);
                        if (user != null) {
                            BotUtil.sendMessage(bot, message, "User created", false, false, null);
                        } else {
                            BotUtil.sendMessage(bot, message, "Failed to create user", false, false, null);
                        }
                        data.clear();
                        return true;
                    }
                    return false;
                }
            }
        } else if (type == Commands.EDIT) {

        } else if (type == Commands.DELETE) {

        } else if (type == Commands.LIST) {

        } else if (type == EditCommands.USERNAME) {
            switch (data.getState()) {
                case 3: {
                    final String username = message.getText();
                    if (username != null && !username.isEmpty()) {
                        data.getUser().setName(username);
                        usersService.update(data.getUser());
                        BotUtil.sendMessage(bot, message, "Done, user's name has been changed", false, false, null);
                        data.clear();
                        return true;
                    }
                    break;
                }
            }
        } else if (type == EditCommands.LEVEL) {
            switch (data.getState()) {
                case 3: {
                    final int level = parseInt(message.getText(), 0);
                    if (level > 0) {
                        data.getUser().setLevel(level);
                        usersService.update(data.getUser());
                        BotUtil.sendMessage(bot, message, "Done, user's level has been changed", false, false, null);
                        data.clear();
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }

    @Override
    protected UserData createData(int ownerId) {
        return new UserData(ownerId);
    }

    private int parseInt(String text, int defaultValue) {
        try {
            int id = Integer.parseInt(text);
            return id;
        } catch (Exception e) {
        }
        return defaultValue;
    }

    static class UserData extends AbstractCommandData {
        private int _id;
        private String _name;
        private User _user;

        public UserData(int ownerId) {
            super(ownerId);
        }

        public int getId() {
            return _id;
        }

        public void setId(int id) {
            _id = id;
        }

        public String getName() {
            return _name;
        }

        public void setName(String name) {
            _name = name;
        }

        public void setUser(User user) {
            _user = user;
        }

        public User getUser() {
            return _user;
        }
    }

    enum Commands implements ICommandType {
        ADD("Add", 0),
        EDIT("Edit", 0),
        DELETE("Delete", 1),
        LIST("List", 1),
        DONE("Done", 2);

        private String _name;
        private int _row;
        private List<ICommandType> _subCommands = new ArrayList<>();

        Commands(String name, int row) {
            _name = name;
            _row = row;
            init();
        }

        protected void init() {
        }

        public String getName() {
            return _name;
        }

        public int getRow() {
            return _row;
        }

        public List<ICommandType> getSubCommands() {
            return _subCommands;
        }
    }

    enum EditCommands implements ICommandType {
        USERNAME("Username", 0),
        LEVEL("Level", 0),
        BACK("Back", 1);

        private String _name;
        private int _row;
        private List<ICommandType> _subCommands = new ArrayList<>();

        EditCommands(String name, int row) {
            _name = name;
            _row = row;
        }

        public String getName() {
            return _name;
        }

        public int getRow() {
            return _row;
        }

        public List<ICommandType> getSubCommands() {
            return _subCommands;
        }
    }
}
