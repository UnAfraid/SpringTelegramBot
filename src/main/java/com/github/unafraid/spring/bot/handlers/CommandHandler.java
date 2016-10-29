package com.github.unafraid.spring.bot.handlers;

import com.github.unafraid.spring.bot.handlers.impl.HelpHandler;
import com.github.unafraid.spring.bot.handlers.impl.ICommandHandler;
import com.github.unafraid.spring.bot.handlers.impl.StartHandler;
import com.github.unafraid.spring.bot.handlers.impl.WhoAmI;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author UnAfraid
 */
public final class CommandHandler {
    private final Map<String, ICommandHandler> _handlers = new ConcurrentHashMap<>();

    protected CommandHandler() {
        // General
        addHandler(new HelpHandler());
        addHandler(new StartHandler());

        // System
        addHandler(new WhoAmI());
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

    public static CommandHandler getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        protected static final CommandHandler INSTANCE = new CommandHandler();
    }
}
