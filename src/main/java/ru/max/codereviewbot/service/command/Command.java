package ru.max.codereviewbot.service.command;

public interface Command {
    // todo Replace user id with more generic execution context
    void execute(long userId, String[] args);
    String getCommandDescription();
}
