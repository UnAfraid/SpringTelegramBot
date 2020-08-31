package com.github.unafraid.spring.bot.handlers.impl;

import com.github.unafraid.spring.model.DBUser;
import com.github.unafraid.spring.services.UserService;
import com.github.unafraid.telegrambot.handlers.inline.*;
import com.github.unafraid.telegrambot.handlers.inline.events.IInlineCallbackEvent;
import com.github.unafraid.telegrambot.handlers.inline.events.IInlineMessageEvent;
import com.github.unafraid.telegrambot.handlers.inline.events.InlineCallbackEvent;
import com.github.unafraid.telegrambot.handlers.inline.events.InlineMessageEvent;
import com.github.unafraid.telegrambot.handlers.inline.layout.InlineFixedButtonsPerRowLayout;
import com.github.unafraid.telegrambot.util.BotUtil;
import com.github.unafraid.telegrambot.util.CommonUtil;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

/**
 * @author UnAfraid
 */
@Service
public class UserMenu extends AbstractInlineHandler {
    private static final String USER_ID_FIELD = "user_id";
    private static final String USER_NAME_FIELD = "user_name";

    private UserService userService;

    public UserMenu(UserService userService) {
        this.userService = userService;
    }

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
        return "Manages telegram users";
    }

    @Override
    public String getCategory() {
        return "Users management";
    }

    @Override
    public int getRequiredAccessLevel() {
        return 10;
    }

    @Override
    public void registerMenu(InlineContext ctx, InlineMenuBuilder builder) {
        builder
                .name("Users menu: ")
                .button(new InlineButtonBuilder(ctx)
                        .name("Add")
                        .row(0)
                        .onQueryCallback(this::handleAddUser)
                        .onInputMessage(this::handleAddUser)
                        .build())
                .button(new InlineButtonBuilder(ctx)
                        .name("Edit")
                        .row(0)
                        .menu(new InlineMenuBuilder(ctx)
                                .name("Edit menu")
                                .button(new InlineButtonBuilder(ctx)
                                        .name("User name")
                                        .row(0)
                                        .onQueryCallback(this::handleEditUserName)
                                        .build())
                                .button(new InlineButtonBuilder(ctx)
                                        .name("Access level")
                                        .row(0)
                                        .onQueryCallback(this::handleEditAccessLevel)
                                        .build())
                                .button(defaultBack(ctx))
                                .build())
                        .build())
                .button(new InlineButtonBuilder(ctx)
                        .name("Delete")
                        .row(1)
                        .onQueryCallback(this::handleDeleteUser)
                        .build())
                .button(new InlineButtonBuilder(ctx)
                        .name("List")
                        .row(1)
                        .onQueryCallback(this::handleListUsers)
                        .build())
                .button(defaultClose(ctx))
                .build();
    }

    private boolean handleAddUser(InlineCallbackEvent event) throws TelegramApiException {
        final InlineUserData userData = event.getContext().getUserData(event.getQuery().getFrom().getId());
        if (userData.getState() == 0) {
            userData.setState(1);
            BotUtil.editMessage(event.getBot(), event.getQuery().getMessage(), "Tell me that user's id", false, null);
            return true;
        }
        return false;
    }

    private boolean handleAddUser(InlineMessageEvent event) throws TelegramApiException {
        final InlineUserData userData = event.getContext().getUserData(event.getMessage().getFrom().getId());
        final AbsSender bot = event.getBot();
        final Message message = event.getMessage();
        switch (userData.getState()) {
            case 1: {
                final String text = message.getText();
                final int id = CommonUtil.parseInt(text, 0);
                if (id == 0) {
                    BotUtil.sendMessage(bot, message, "User id not set, will be automatically mapped once user send me a message!", false, false, null);
                } else if (id < 0) {
                    BotUtil.sendMessage(bot, message, "Negative ids are not supported, either 0 for dynamic mapping or > 0 with initial id!", false, false, null);
                    return false;
                }
                userData.setState(2);
                userData.getParams().put(USER_ID_FIELD, id);
                BotUtil.sendMessage(bot, message, "Great, now give me the user name", false, false, null);
                return true;
            }
            case 2: {
                final String text = message.getText();
                if ((text == null) || text.isEmpty()) {
                    return true;
                }
                userData.setState(3);
                userData.getParams().put(USER_NAME_FIELD, text);
                BotUtil.sendMessage(bot, message, "What access level shall i set to " + text + " ?", false, false, null);
                return true;
            }
            case 3: {
                final String text = message.getText();
                final int level = CommonUtil.parseInt(text, 0);
                if (level <= 0) {
                    BotUtil.sendMessage(bot, message, "Access level must be positive number!", false, false, null);
                    return true;
                }

                final DBUser creator = userService.findById(message.getFrom().getId());
                if (creator.getLevel() <= level) {
                    BotUtil.sendMessage(bot, message, "You cannot create user with higher access then yours, try again!", false, false, null);
                    return true;
                }

                final DBUser createdUser = userService.create(userData.getParams().getInt(USER_ID_FIELD), userData.getParams().getString(USER_NAME_FIELD), level);
                if (createdUser != null) {
                    BotUtil.sendMessage(bot, message, "User created", false, false, null);
                } else {
                    BotUtil.sendMessage(bot, message, "Failed to create user", false, false, null);
                }
                event.getContext().clear(message.getFrom().getId());
                return true;
            }
        }
        return false;
    }

    private boolean handleEditUserName(InlineCallbackEvent event) throws TelegramApiException {
        final IInlineCallbackEvent onQueryCallback = evt ->
        {
            final InlineUserData userData = evt.getContext().getUserData(evt.getQuery().getFrom().getId());
            if (userData.getState() == 0) {
                userData.setState(1);
                userData.getParams().put(USER_NAME_FIELD, userData.getActiveButton().getName());
                BotUtil.editMessage(evt.getBot(), evt.getQuery().getMessage(), "What should be the new Username?", false, null);
                return true;
            }
            return false;
        };

        final IInlineMessageEvent onInputMessage = evt ->
        {
            final InlineUserData userData = evt.getContext().getUserData(evt.getMessage().getFrom().getId());
            if (userData.getState() == 1) {
                final String username = evt.getMessage().getText();
                if ((username == null) || username.isEmpty()) {
                    BotUtil.sendMessage(evt.getBot(), evt.getMessage(), "You need to provide non empty string for username!", false, false, null);
                    return true;
                }

                final String userName = userData.getParams().getString(USER_NAME_FIELD);
                final DBUser userToEdit = userService.findByName(userName);
                final DBUser creator = userService.findById(evt.getMessage().getFrom().getId());
                if (creator.getLevel() <= userToEdit.getLevel()) {
                    BotUtil.sendMessage(evt.getBot(), evt.getMessage(), "You cannot edit user with higher access then yours, try again!", false, false, null);
                    return true;
                }
                userToEdit.setName(username);
                userService.update(userToEdit);
                BotUtil.sendMessage(evt.getBot(), evt.getMessage(), "Done, user's name has been changed", false, false, null);
                evt.getContext().clear(evt.getMessage().getFrom().getId());
                return true;
            }
            return false;
        };

        final List<DBUser> users = userService.findAll();
        final InlineUserData userData = event.getContext().getUserData(event.getQuery().getFrom().getId());
        final InlineMenuBuilder usersBuilder = new InlineMenuBuilder(event.getContext(), userData.getActiveMenu());
        usersBuilder.name("Select user to edit");
        for (DBUser user : users) {
            usersBuilder.button(new InlineButtonBuilder(event.getContext())
                    .name(user.getName())
                    .onQueryCallback(onQueryCallback)
                    .onInputMessage(onInputMessage)
                    .build());
        }

        // Back button
        usersBuilder.button(defaultBack(event.getContext()));

        final InlineMenu usersMenu = usersBuilder.build();
        userData.editCurrentMenu(event.getBot(), event.getQuery().getMessage(), new InlineFixedButtonsPerRowLayout(3), usersMenu);
        return true;
    }

    private boolean handleEditAccessLevel(InlineCallbackEvent event) throws TelegramApiException {
        final IInlineCallbackEvent onQueryCallback = evt ->
        {
            final InlineUserData userData = evt.getContext().getUserData(evt.getQuery().getFrom().getId());
            if (userData.getState() == 0) {
                userData.setState(1);
                userData.getParams().put(USER_NAME_FIELD, userData.getActiveButton().getName());
                BotUtil.editMessage(evt.getBot(), evt.getQuery().getMessage(), "What should be the new access level?", false, null);
                return true;
            }
            return false;
        };

        final IInlineMessageEvent onInputMessage = evt ->
        {
            final InlineUserData userData = evt.getContext().getUserData(evt.getMessage().getFrom().getId());
            if (userData.getState() == 1) {
                final String accessLevelString = evt.getMessage().getText();
                if ((accessLevelString == null) || accessLevelString.isEmpty()) {
                    BotUtil.sendMessage(evt.getBot(), evt.getMessage(), "You need to provide non empty string for username!", false, false, null);
                    return true;
                } else if (!CommonUtil.isDigit(accessLevelString)) {
                    BotUtil.sendMessage(evt.getBot(), evt.getMessage(), "Access level should be positive integer", false, false, null);
                    return true;
                }

                final int accessLevel = CommonUtil.parseInt(accessLevelString, 0);
                final String userName = userData.getParams().getString(USER_NAME_FIELD);
                final DBUser userToEdit = userService.findByName(userName);
                final DBUser creator = userService.findById(evt.getMessage().getFrom().getId());
                if (creator.getLevel() <= userToEdit.getLevel()) {
                    BotUtil.sendMessage(evt.getBot(), evt.getMessage(), "You cannot edit user with higher access then yours, try again!", false, false, null);
                    return true;
                }
                userToEdit.setLevel(accessLevel);
                userService.update(userToEdit);
                BotUtil.sendMessage(evt.getBot(), evt.getMessage(), "Done, user's access level has been changed", false, false, null);
                evt.getContext().clear(evt.getMessage().getFrom().getId());
                return true;
            }
            return false;
        };

        final List<DBUser> users = userService.findAll();
        final InlineUserData userData = event.getContext().getUserData(event.getQuery().getFrom().getId());
        final InlineMenuBuilder usersBuilder = new InlineMenuBuilder(event.getContext(), userData.getActiveMenu());
        usersBuilder.name("Select user:");
        for (DBUser user : users) {
            //@formatter:off
            usersBuilder.button(new InlineButtonBuilder(event.getContext())
                    .name(user.getName())
                    .onQueryCallback(onQueryCallback)
                    .onInputMessage(onInputMessage)
                    .build());
            //@formatter:on
        }

        // Back button
        usersBuilder.button(defaultBack(event.getContext()));

        final InlineMenu usersMenu = usersBuilder.build();
        userData.editCurrentMenu(event.getBot(), event.getQuery().getMessage(), new InlineFixedButtonsPerRowLayout(3), usersMenu);
        return true;
    }

    private boolean handleListUsers(InlineCallbackEvent event) throws TelegramApiException {
        final IInlineCallbackEvent onQueryCallback = evt ->
        {
            final InlineUserData userData = evt.getContext().getUserData(evt.getQuery().getFrom().getId());
            final String targetUserName = userData.getActiveButton().getName();
            final DBUser usr = userService.findByName(targetUserName);
            final AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setText(usr != null ? String.format("User: [%d](%s) Level: %d", usr.getId(), usr.getName(), usr.getLevel()) : String.format("U've clicked at %s", evt.getQuery().getData()));
            answer.setCallbackQueryId(evt.getQuery().getId());
            answer.setShowAlert(true);
            evt.getBot().execute(answer);
            return true;
        };

        final List<DBUser> users = userService.findAll();
        final InlineUserData userData = event.getContext().getUserData(event.getQuery().getFrom().getId());
        final InlineMenuBuilder usersBuilder = new InlineMenuBuilder(event.getContext(), userData.getActiveMenu());
        usersBuilder.name("Select user");
        for (DBUser user : users) {
            usersBuilder.button(new InlineButtonBuilder(event.getContext())
                    .name(user.getName())
                    .onQueryCallback(onQueryCallback)
                    .build());
        }

        // Back button
        usersBuilder.button(defaultBack(event.getContext()));

        final InlineMenu usersMenu = usersBuilder.build();
        userData.editCurrentMenu(event.getBot(), event.getQuery().getMessage(), new InlineFixedButtonsPerRowLayout(3), usersMenu);
        return true;
    }

    private boolean handleDeleteUser(InlineCallbackEvent event) throws TelegramApiException {
        final IInlineCallbackEvent onQueryCallback = evt ->
        {
            final int id = evt.getQuery().getFrom().getId();
            final InlineUserData userData = evt.getContext().getUserData(id);
            final String targetUserName = userData.getActiveButton().getName();
            final DBUser userToDelete = userService.findByName(targetUserName);
            final DBUser currentUser = userService.findById(id);
            if (currentUser.getLevel() <= userToDelete.getLevel()) {
                BotUtil.sendMessage(evt.getBot(), evt.getQuery().getMessage(), "You cannot delete user with higher access then yours, try again!", false, false, null);
                return true;
            }

            userService.delete(userToDelete.getId());
            BotUtil.editMessage(evt.getBot(), evt.getQuery().getMessage(), String.format("User %s deleted", userToDelete.getName()), false, null);
            evt.getContext().clear(id);
            return true;
        };

        final List<DBUser> users = userService.findAll();
        final InlineUserData userData = event.getContext().getUserData(event.getQuery().getFrom().getId());
        final InlineMenuBuilder usersBuilder = new InlineMenuBuilder(event.getContext(), userData.getActiveMenu());
        usersBuilder.name("Select user to delete");
        for (DBUser user : users) {
            usersBuilder.button(new InlineButtonBuilder(event.getContext())
                    .name(user.getName())
                    .onQueryCallback(onQueryCallback)
                    .build());
        }

        // Back button
        usersBuilder.button(defaultBack(event.getContext()));

        final InlineMenu usersMenu = usersBuilder.build();
        userData.editCurrentMenu(event.getBot(), event.getQuery().getMessage(), usersMenu.getName(), new InlineFixedButtonsPerRowLayout(3), usersMenu);
        return true;
    }
}
