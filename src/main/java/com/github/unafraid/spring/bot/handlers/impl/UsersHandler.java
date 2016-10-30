package com.github.unafraid.spring.bot.handlers.impl;

import com.github.unafraid.spring.bot.db.model.User;
import com.github.unafraid.spring.bot.db.services.IUsersService;
import com.github.unafraid.spring.bot.util.BotUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.List;


/**
 * @author UnAfraid
 */
@Service
public final class UsersHandler implements ICommandHandler {

    @Autowired
    private IUsersService usersService;

    public UsersHandler() {
    }

    @Override
    public String getCommand() {
        return "/users";
    }

    @Override
    public String getUsage() {
        return "/users add <id> <username> <level>|changeLevel <id> <level>|remove <id>|list";
    }

    @Override
    public String getDescription() {
        return "Manipulates authorized users";
    }

    @Override
    public int getRequiredAccessLevel() {
        return 7;
    }

    @Override
    public void onMessage(AbsSender bot, Message message, int updateId, List<String> args) throws TelegramApiException {
        if (args.isEmpty()) {
            BotUtil.sendUsage(bot, message, this);
            return;
        }

        final String command = args.get(0);
        switch (command) {
            case "add": {
                if (args.size() < 4) {
                    BotUtil.sendMessage(bot, message, "/users add <id> <name> <level>", false, false, null);
                    return;
                }

                final int id = BotUtil.parseInt(args.get(1), -1);
                if (id == -1) {
                    BotUtil.sendMessage(bot, message, "invalid id specified", false, false, null);
                    return;
                }
                String username = args.get(2);
                int level = BotUtil.parseInt(args.get(3), 0);
                if (username.charAt(0) == '@') {
                    username = username.substring(1);
                } else if (level <= 0) {
                    BotUtil.sendMessage(bot, message, "/users add <id> <name> <level>", false, false, null);
                    return;
                }

                if (usersService.findById(id) != null) {
                    BotUtil.sendMessage(bot, message, username + " is already authorized use \"/users changeLevel\" instead!", false, false, null);
                    break;
                }

                final User user = usersService.create(id, username, level);
                if (user != null) {
                    BotUtil.sendMessage(bot, message, username + " is now authorized!", true, false, null);
                } else {
                    BotUtil.sendMessage(bot, message, "Failed to create user", true, false, null);
                }
                break;
            }
            case "changeLevel": {
                if (args.size() < 3) {
                    BotUtil.sendMessage(bot, message, "/users changeLevel <id> <level>", false, false, null);
                    return;
                }

                final int id = BotUtil.parseInt(args.get(1), -1);
                if (id == -1) {
                    BotUtil.sendMessage(bot, message, "invalid id specified", false, false, null);
                    return;
                }

                int level = BotUtil.parseInt(args.get(2), 0);
                if (level <= 0) {
                    BotUtil.sendMessage(bot, message, "/users changeLevel <id> <level>", false, false, null);
                    return;
                }

                final User user = usersService.findById(id);
                if (user == null) {
                    BotUtil.sendMessage(bot, message, "There is no user with such id", false, false, null);
                    return;
                }

                user.setLevel(level);
                BotUtil.sendMessage(bot, message, user.getName() + " has been updated!", false, false, null);
                break;
            }
            case "remove": {
                if (args.size() < 2) {
                    BotUtil.sendMessage(bot, message, "/users remove <id>", false, false, null);
                    return;
                }

                final int id = BotUtil.parseInt(args.get(1), -1);
                if (id == -1) {
                    BotUtil.sendMessage(bot, message, "invalid id specified", false, false, null);
                    return;
                }

                final User user = usersService.findById(id);
                if (user == null) {
                    BotUtil.sendMessage(bot, message, "There is no user with such id", false, false, null);
                    break;
                }
                usersService.delete(id);
                BotUtil.sendMessage(bot, message, user.getName() + " is no longer authorized!", true, false, null);
                break;
            }
            case "list": {
                final StringBuilder sb = new StringBuilder();
                sb.append("Authorized users:").append(System.lineSeparator());
                for (User user : usersService.findAll()) {
                    sb.append(" - ").append(user.getId()).append(" @").append(user.getName()).append(" level: ").append(user.getLevel()).append(System.lineSeparator());
                }
                BotUtil.sendMessage(bot, message, sb.toString(), true, false, null);
                break;
            }
            default: {
                BotUtil.sendUsage(bot, message, this);
                break;
            }
        }
    }
}
