package com.github.unafraid.spring.services;

import com.github.unafraid.spring.model.DBUser;
import com.github.unafraid.spring.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author UnAfraid
 */
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Transactional
    public DBUser create(int id, String name, int level) {
        final DBUser user = new DBUser(id, name, level);
        return userRepository.save(user);
    }

    @Transactional
    public void update(DBUser user) {
        userRepository.save(user);
    }

    @Transactional
    public void delete(int id) {
        userRepository.findById(id).ifPresent(userRepository::delete);
    }

    @Transactional
    public DBUser findById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public DBUser findByName(String name) {
        return userRepository.findByName(name);
    }

    public List<DBUser> findAll() {
        return userRepository.findAll();
    }

    public long count() {
        return userRepository.count();
    }

    public boolean validate(int id, int level) {
        if (level == 0) {
            return true;
        }

        final DBUser user = findById(id);
        return (user != null) && (user.getLevel() >= level);
    }
}
