package com.github.unafraid.spring.bot.handlers.general;

import com.github.unafraid.spring.services.UsersService;

/**
 * @author UnAfraid
 */
public interface IAccessLevelHandler {
    /**
     * @return The access level required to execute this command
     */
    default int getRequiredAccessLevel() {
        return 0;
    }

    static boolean validate(Object handler, int id, UsersService usersService) {
        if (handler instanceof IAccessLevelHandler) {
            return usersService.validate(id, ((IAccessLevelHandler) handler).getRequiredAccessLevel());
        }
        return true;
    }
}
