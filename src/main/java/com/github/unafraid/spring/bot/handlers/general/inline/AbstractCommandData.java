package com.github.unafraid.spring.bot.handlers.general.inline;

/**
 * Created by UnAfraid on 30.10.2016 г..
 */
public abstract class AbstractCommandData {
    private final int _ownerId;
    private int _state;
    private IInlineCommandType _type;

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

    public final IInlineCommandType getType() {
        return _type;
    }

    public final void setType(IInlineCommandType type) {
        _type = type;
    }

    public final void clear() {
        _state = 0;
        _type = null;
    }
}
