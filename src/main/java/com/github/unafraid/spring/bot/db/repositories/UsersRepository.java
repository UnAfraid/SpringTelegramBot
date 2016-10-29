package com.github.unafraid.spring.bot.db.repositories;

import com.github.unafraid.spring.bot.db.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by UnAfraid on 29.10.2016 г..
 */
public interface UsersRepository extends JpaRepository<User, Integer> {
}
