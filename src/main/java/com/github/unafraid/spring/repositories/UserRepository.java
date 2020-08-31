package com.github.unafraid.spring.repositories;

import com.github.unafraid.spring.model.DBUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * @author UnAfraid
 */
public interface UserRepository extends JpaRepository<DBUser, Integer> {
    @Query("SELECT u FROM DBUser u WHERE u.name = ?1")
    DBUser findByName(String name);
}
