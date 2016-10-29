package com.github.unafraid.spring.bot.db.services;

import com.github.unafraid.spring.bot.db.model.User;

import java.util.List;

/**
 * @author UnAfraid
 */
public interface IUsersService {
    User create(int id, String name, int level);

    User delete(int id);

    User findById(int id);

    List<User> findAll();
}
