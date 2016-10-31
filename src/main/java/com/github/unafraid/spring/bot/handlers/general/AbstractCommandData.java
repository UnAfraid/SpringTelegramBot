package com.github.unafraid.spring.bot.handlers.general;

/**
 * Created by UnAfraid on 30.10.2016 г..
 */
public abstract class AbstractCommandData {
    private int _ownerId;
    private int _state;
    private ICommandType _type;

    public AbstractCommandData(int ownerId) {
        _ownerId = ownerId;
    }

    public final int getOwnerId() {
        return _ownerId;
    }

    public final int getState() {
        return _state;
    }

    public final void setState(int state) {
        _state = state;
    }

    public final ICommandType getType() {
        return _type;
    }

    public final void setType(ICommandType type) {
        _type = type;
    }

    public void clear() {
        _state = 0;
        _type = null;
    }
}
