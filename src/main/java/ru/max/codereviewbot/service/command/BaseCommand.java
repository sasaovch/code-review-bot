package ru.max.codereviewbot.service.command;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.NewMessageBody;
import ru.max.codereviewbot.domain.User;

abstract public class BaseCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(BaseCommand.class);

    private final TamTamBotAPI maxApi;

    protected BaseCommand(TamTamBotAPI maxApi) {
        this.maxApi = maxApi;
    }

    protected void trySendMessage(long userId, String message) {
        try {
            this.maxApi.sendMessage(new NewMessageBody(message, null, null))
                .userId(userId)
                .execute();
        } catch (APIException | ClientException e) {
            log.error(e.getMessage(), e);
        }
    }

    protected void trySendMessage(List<User> reviewers, String message) {
        try {
            for (User user : reviewers) {
                this.maxApi.sendMessage(new NewMessageBody(message, null, null))
                    .userId(user.id())
                    .execute();
            }
        } catch (APIException | ClientException e) {
            log.error(e.getMessage(), e);
        }
    }

    protected Optional<String> shouldBeTwoArg(String[] args) {
        if (args.length != 2) {
            return Optional.of("Incorrect number of arguments (expected 2, got " + args.length + ")");
        }

        return Optional.empty();
    }

    protected Optional<String> shouldBeOneArg(String[] args) {
        if (args.length != 1) {
            return Optional.of("Incorrect number of arguments (expected 1, got " + args.length + ")");
        }

        return Optional.empty();
    }

    protected Optional<String> shouldBeValidUrl(String url) {
        try {
            new URL(url).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            return Optional.of("Incorrect URL");
        }
        return Optional.empty();
    }

    protected Optional<String> validateArgsAsUrlOrId(String[] args) {
        Optional<String> validationArgs = shouldBeOneArg(args);
        if (validationArgs.isPresent()) {
            return validationArgs;
        }

        if (args[0].chars().allMatch(Character::isDigit)) {
            return Optional.empty();
        }
        return shouldBeValidUrl(args[0]);
    }

    protected Optional<String> validateArgsAsUrl(String[] args) {
        Optional<String> validationArgs = shouldBeOneArg(args);
        if (validationArgs.isPresent()) {
            return validationArgs;
        }

        return shouldBeValidUrl(args[0]);
    }
}
