package com.github.unafraid.spring.bot.db.services;

import com.github.unafraid.spring.bot.db.model.User;

import java.util.List;

/**
 * @author UnAfraid
 */
public interface IUsersService {
    User create(int id, String name, int level);

    void update(User user);

    User delete(int id);

    User findById(int id);

    List<User> findAll();

    default boolean validate(int id, int level) {
        if (level == 0) {
            return true;
        }

        final User user = findById(id);
        return (user != null) && (user.getLevel() >= level);
    }
}
