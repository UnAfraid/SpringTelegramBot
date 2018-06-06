package com.github.unafraid.spring.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.github.unafraid.spring.model.User;

/**
 * @author UnAfraid
 */
public interface UsersRepository extends JpaRepository<User, Integer> {
	@Query("SELECT u FROM User u WHERE u.name = ?1")
	User findByName(String name);
}
