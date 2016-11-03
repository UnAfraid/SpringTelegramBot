package com.github.unafraid.spring.services;

import com.github.unafraid.spring.model.User;
import com.github.unafraid.spring.repositories.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author UnAfraid
 */
@Service
public class UsersService {

    @Resource
    private UsersRepository usersRepository;

    @Transactional
    public User create(int id, String name, int level) {
        final User user = new User(id, name, level);
        return usersRepository.save(user);
    }

    @Transactional
    public void update(User user) {
        usersRepository.save(user);
    }


    @Transactional
    public User delete(int id) {
        final User user = usersRepository.findOne(id);
        if (user == null) {
            return null;
        }
        usersRepository.delete(user);
        return user;
    }

    @Transactional
    public User findById(int id) {
        return usersRepository.findOne(id);
    }

    @Transactional
    public User findByName(String name) {
        return usersRepository.findByName(name);
    }

    public List<User> findAll() {
        return usersRepository.findAll();
    }

    public boolean validate(int id, int level) {
        if (level == 0) {
            return true;
        }

        final User user = findById(id);
        return (user != null) && (user.getLevel() >= level);
    }
}
