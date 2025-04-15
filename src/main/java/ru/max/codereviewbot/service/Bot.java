package ru.max.codereviewbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.max.codereviewbot.domain.User;
import ru.max.codereviewbot.persist.UserRepository;
import ru.max.codereviewbot.service.command.Command;

import java.util.ArrayList;

@Service
public class Bot {

    private final UserRepository userRepository;

    @Autowired
    public Bot(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void start(long userId, String name, String username) {
        if (userRepository.get(userId).isEmpty()) {
            userRepository.save(new User(userId, name, username));
        }
    }

    public void executeCommand(long userId, Command command, String[] args) {
        command.execute(userId, args);
    }
}
