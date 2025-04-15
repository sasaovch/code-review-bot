package ru.max.codereviewbot.service.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import chat.tamtam.botapi.TamTamBotAPI;
import ru.max.codereviewbot.domain.PullRequest;
import ru.max.codereviewbot.domain.PullRequestStatus;
import ru.max.codereviewbot.domain.User;
import ru.max.codereviewbot.persist.PullRequestRepository;
import ru.max.codereviewbot.persist.UserRepository;
import ru.max.codereviewbot.service.PullRequestService;

@Component
public class StartReviewCommand extends BaseCommand {

    private static final Logger log = LoggerFactory.getLogger(StartReviewCommand.class);

    private static final int REVIEWERS_COUNT = 1;

    private final UserRepository userRepository;
    private final PullRequestService pullRequestService;

    public StartReviewCommand(TamTamBotAPI maxApi, UserRepository userRepository, PullRequestService pullRequestService) {
        super(maxApi);
        this.userRepository = userRepository;
        this.pullRequestService = pullRequestService;
    }

    @Override
    public String getCommandDescription() {
        return "/start_review {url} - starts review process";
    }

    @Override
    public void execute(long userId, String[] args) {
        Optional<String> validationResult = validateArgsAsUrl(args);
        if (validationResult.isPresent()) {
            trySendMessage(userId, validationResult.get());
            return;
        }

        User author = userRepository.get(userId).orElseThrow();
        List<User> reviewers = getRandomReviewers(author);

        if (reviewers.isEmpty()) {
            trySendMessage(author.id(), "No available reviewers");
            return;
        }

        PullRequest pullRequest = new PullRequest(
            PullRequestRepository.nextId(),
            args[0],
            PullRequestStatus.WAITING_FOR_REVIEW,
            author.id(),
            reviewers.stream().mapToLong(User::id).boxed().toList()
        );

        pullRequestService.save(pullRequest);

        trySendMessage(author.id(), "Pull request submitted for review");
        trySendMessage(reviewers, "New PR for review: " + pullRequest.url());
    }

    private List<User> getRandomReviewers(User author) {
        List<User> candidates = new ArrayList<>(
            userRepository.findAll().stream().filter(user -> !author.id().equals(user.id())).toList());

        if (candidates.size() < REVIEWERS_COUNT) {
            return List.of();
        }

        Collections.shuffle(candidates);
        return candidates.subList(0, REVIEWERS_COUNT);
    }
}
