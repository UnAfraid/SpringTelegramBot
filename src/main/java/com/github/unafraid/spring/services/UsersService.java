package com.github.unafraid.spring.services;

import java.util.List;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.unafraid.spring.model.DBUser;
import com.github.unafraid.spring.repositories.UsersRepository;

/**
 * @author UnAfraid
 */
@Service
public class UsersService {

	@Resource
	private UsersRepository usersRepository;

	@Transactional
	public DBUser create(int id, String name, int level) {
		final DBUser user = new DBUser(id, name, level);
		return usersRepository.save(user);
	}

	@Transactional
	public void update(DBUser user) {
		usersRepository.save(user);
	}


	@Transactional
	public DBUser delete(int id) {
		final DBUser user = usersRepository.findOne(id);
		if (user == null) {
			return null;
		}
		usersRepository.delete(user);
		return user;
	}

	@Transactional
	public DBUser findById(int id) {
		return usersRepository.findOne(id);
	}

	@Transactional
	public DBUser findByName(String name) {
		return usersRepository.findByName(name);
	}

	public List<DBUser> findAll() {
		return usersRepository.findAll();
	}

	public boolean validate(int id, int level) {
		if (level == 0) {
			return true;
		}

		final DBUser user = findById(id);
		return (user != null) && (user.getLevel() >= level);
	}
}
