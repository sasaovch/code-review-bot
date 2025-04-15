package ru.max.codereviewbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.max.codereviewbot.service.command.Command;
import ru.max.codereviewbot.service.command.UnknownCommand;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Component
public class CommandReader {

    private final Map<String, Command> commands;
    private final UnknownCommand unknownCommand;

    @Autowired
    CommandReader(@Qualifier("namesToCommands") Map<String, Command> commands, UnknownCommand unknownCommand) {
        this.commands = commands;
        this.unknownCommand = unknownCommand;
    }

    public Optional<CommandAndArgs> read(String message) {
        if (!containsCommand(message)) {
            return Optional.empty();
        }

        String[] split = message.split(" ");

        String commandName = split[0].substring(1);
        Command command = commands.getOrDefault(commandName, unknownCommand);

        String[] args = getArgs(split);

        return Optional.of(new CommandAndArgs(command, args));
    }

    private boolean containsCommand(String message) {
        return message.startsWith("/");
    }

    private String[] getArgs(String[] split) {
        return Arrays.copyOfRange(split, 1, split.length);
    }
}
