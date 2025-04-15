package ru.max.codereviewbot.service;

import ru.max.codereviewbot.service.command.Command;

public record CommandAndArgs(Command command, String[] args) {
}
