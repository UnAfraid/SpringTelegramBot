package com.github.unafraid.spring.bot.handlers.general;

import java.util.List;

/**
 * Created by UnAfraid on 30.10.2016 г..
 */
public interface ICommandType {
    String getName();

    int getRow();

    List<ICommandType> getSubCommands();
}
