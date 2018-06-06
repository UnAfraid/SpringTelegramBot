package com.github.unafraid.spring.bot.handlers.general;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.unafraid.spring.services.UsersService;

/**
 * @author UnAfraid
 */
public final class CommandHandler {
	private final Map<String, ICommandHandler> _handlers = new ConcurrentHashMap<>();

	protected CommandHandler() {
	}

	public void addHandler(ICommandHandler handler) {
		_handlers.put(handler.getCommand(), handler);
	}

	public ICommandHandler getHandler(String command) {
		return _handlers.get(command);
	}

	public Collection<ICommandHandler> getHandlers() {
		return _handlers.values();
	}

	public <T> List<T> getHandlers(Class<T> clazz, int id, UsersService usersService) {
		return _handlers.values().stream()
				.filter(clazz::isInstance)
				.map(clazz::cast)
				.filter(messageHandler -> IAccessLevelHandler.validate(messageHandler, id, usersService))
				.collect(Collectors.toList());
	}

	public static CommandHandler getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder {
		protected static final CommandHandler INSTANCE = new CommandHandler();
	}
}
