package com.github.unafraid.spring.bot.db.services;

import com.github.unafraid.spring.bot.db.model.User;
import com.github.unafraid.spring.bot.db.repositories.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by UnAfraid on 29.10.2016 г..
 */
@Service
public class UsersServiceImpl implements IUsersService {

    @Resource
    private UsersRepository usersRepository;

    @Override
    @Transactional
    public User create(int id, String name, int level) {
        final User user = new User(id, name, level);
        return usersRepository.save(user);
    }

    @Override
    @Transactional
    public void update(User user) {
        usersRepository.save(user);
    }


    @Override
    @Transactional
    public User delete(int id) {
        final User user = usersRepository.findOne(id);
        if (user == null) {
            return null;
        }
        usersRepository.delete(user);
        return user;
    }

    @Override
    @Transactional
    public User findById(int id) {
        return usersRepository.findOne(id);
    }

    @Override
    public List<User> findAll() {
        return usersRepository.findAll();
    }
}
