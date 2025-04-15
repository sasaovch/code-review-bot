package ru.max.codereviewbot.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import chat.tamtam.botapi.TamTamBotAPI;
import ru.max.codereviewbot.persist.UserRepository;
import ru.max.codereviewbot.service.command.ApproveReviewCommand;
import ru.max.codereviewbot.service.command.Command;
import ru.max.codereviewbot.service.command.HelpCommand;
import ru.max.codereviewbot.service.command.ShowMyReviewsCommand;
import ru.max.codereviewbot.service.command.StartReviewCommand;
import ru.max.codereviewbot.service.command.SubmitReviewCommand;

@Configuration
public class CommandConfig {

    private final TamTamBotAPI maxApi;

    private final UserRepository userRepository;
    private final PullRequestService pullRequestService;

    @Autowired
    public CommandConfig(TamTamBotAPI maxApi, UserRepository userRepository, PullRequestService pullRequestService) {
        this.maxApi = maxApi;
        this.userRepository = userRepository;
        this.pullRequestService = pullRequestService;
    }

    @Bean
    Map<String, Command> namesToCommands() {
        Map<String, Command> commandMap = new HashMap<>();

        commandMap.put("start_review", new StartReviewCommand(maxApi, userRepository, pullRequestService));
        commandMap.put("submit_review", new SubmitReviewCommand(maxApi, userRepository, pullRequestService));
        commandMap.put("approve_review", new ApproveReviewCommand(maxApi, userRepository, pullRequestService));
        commandMap.put("show_my_reviews", new ShowMyReviewsCommand(maxApi, userRepository, pullRequestService));
        commandMap.put("help", new HelpCommand(commandMap, maxApi));

        return Collections.unmodifiableMap(commandMap);
    }
}
