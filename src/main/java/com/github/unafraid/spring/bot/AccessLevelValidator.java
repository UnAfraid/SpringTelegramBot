package com.github.unafraid.spring.bot;

import com.github.unafraid.spring.services.UserService;
import com.github.unafraid.telegrambot.handlers.IAccessLevelValidator;
import com.github.unafraid.telegrambot.handlers.ITelegramHandler;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

/**
 * @author UnAfraid
 */
@Service
public class AccessLevelValidator implements IAccessLevelValidator {
    private final UserService userService;

    public AccessLevelValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean validate(ITelegramHandler handler, User user) {
        return userService.validate(user.getId(), handler.getRequiredAccessLevel());
    }
}
