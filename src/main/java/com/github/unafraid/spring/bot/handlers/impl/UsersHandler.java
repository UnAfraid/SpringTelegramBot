package com.github.unafraid.spring.bot.handlers.impl;

import com.github.unafraid.spring.bot.handlers.general.inline.AbstractCommandData;
import com.github.unafraid.spring.bot.handlers.general.inline.AbstractInlineMenu;
import com.github.unafraid.spring.bot.handlers.general.inline.IInlineCommandType;
import com.github.unafraid.spring.bot.util.BotUtil;
import com.github.unafraid.spring.model.User;
import com.github.unafraid.spring.services.UsersService;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author UnAfraid
 */
@Service
public class UsersHandler extends AbstractInlineMenu<UsersHandler.UserData> {
    @Inject
    private UsersService usersService;

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

    UsersHandler() {
        // Add Commands
        Arrays.stream(Commands.values()).forEach(this::addCommand);
    }

    @Override
    protected boolean onMenuSelect(AbsSender bot, Update update, CallbackQuery query, UserData data) throws TelegramApiException {
        final IInlineCommandType type = data.getType();
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
                    sendUserButtons(bot, query);
                    data.setState(1);
                    return true;
                }
                case 1: {
                    final User userToEdit = usersService.findByName(query.getData());
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
                    sendUserButtons(bot, query);
                    data.setState(1);
                    return true;
                }
                case 1: {
                    if ("Back".equalsIgnoreCase(query.getData())) {
                        back(bot, query.getMessage(), data);
                        return true;
                    }

                    final User userToDelete = usersService.findByName(query.getData());
                    if (userToDelete != null) {
                        if (usersService.delete(userToDelete.getId()) != null) {
                            BotUtil.editMessage(bot, query.getMessage(), "User " + userToDelete.getName() + " has been deleted", false, null);
                        } else {
                            BotUtil.editMessage(bot, query.getMessage(), "Failed to delete " + query.getData(), false, null);
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
                    sendUserButtons(bot, query);
                    data.setState(1);
                    return true;
                }
                case 1: {
                    if ("Back".equalsIgnoreCase(query.getData())) {
                        back(bot, query.getMessage(), data);
                        return true;
                    }
                    final User user = usersService.findByName(query.getData());
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

    private void sendUserButtons(AbsSender bot, CallbackQuery query) throws TelegramApiException {
        final InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        final List<List<InlineKeyboardButton>> buttons = markup.getKeyboard();
        usersService.findAll().forEach(user ->
        {
            if (buttons.isEmpty() || (buttons.get(buttons.size() - 1).size() >= 3)) {
                buttons.add(new ArrayList<>());
            }
            buttons.get(buttons.size() - 1).add(BotUtil.createButton(user.getName()));
        });
        markup.getKeyboard().add(Collections.singletonList(BotUtil.createButton("Back")));
        BotUtil.editMessage(bot, query.getMessage(), "Tell me that user's id", false, markup);
    }

    @Override
    protected boolean onMessageInput(AbsSender bot, Message message, UserData data) throws TelegramApiException {
        final IInlineCommandType type = data.getType();
        if (type == Commands.ADD) {
            switch (data.getState()) {
                case 1: {
                    final String text = message.getText();
                    final int id = BotUtil.parseInt(text, 0);
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
                    final int level = BotUtil.parseInt(text, 0);
                    if (level > 0) {
                        final User creator = usersService.findById(message.getFrom().getId());
                        if (creator == null) {
                            return false; // Shouldn't happen like ever..
                        }

                        if (creator.getLevel() < level) {
                            BotUtil.sendMessage(bot, message, "You cannot create user with higher access then yours, try again!", false, false, null);
                            return true;
                        }

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
                    final int level = BotUtil.parseInt(message.getText(), 0);
                    if (level > 0) {
                        final User creator = usersService.findById(message.getFrom().getId());
                        if (creator == null) {
                            return false; // Shouldn't happen like ever..
                        }

                        if (creator.getLevel() < level) {
                            BotUtil.sendMessage(bot, message, "You cannot create user with higher access then yours, try again!", false, false, null);
                            return true;
                        }

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

    static class UserData extends AbstractCommandData {
        private int _id;
        private String _name;
        private User _user;

        UserData(int ownerId) {
            super(ownerId);
        }

        int getId() {
            return _id;
        }

        void setId(int id) {
            _id = id;
        }

        String getName() {
            return _name;
        }

        void setName(String name) {
            _name = name;
        }

        void setUser(User user) {
            _user = user;
        }

        User getUser() {
            return _user;
        }
    }

    private enum Commands implements IInlineCommandType {
        ADD("Add", 0),
        EDIT("Edit", 0),
        DELETE("Delete", 1),
        LIST("List", 1),
        DONE("Done", 2);

        private String _name;
        private int _row;
        private List<IInlineCommandType> _subCommands = new ArrayList<>();

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

        public List<IInlineCommandType> getSubCommands() {
            return _subCommands;
        }
    }

    private enum EditCommands implements IInlineCommandType {
        USERNAME("Username", 0),
        LEVEL("Level", 0),
        BACK("Back", 1);

        private String _name;
        private int _row;
        private List<IInlineCommandType> _subCommands = new ArrayList<>();

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

        public List<IInlineCommandType> getSubCommands() {
            return _subCommands;
        }
    }
}
