package ru.max.codereviewbot.service.command;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import chat.tamtam.botapi.TamTamBotAPI;
import ru.max.codereviewbot.domain.PullRequest;
import ru.max.codereviewbot.domain.User;
import ru.max.codereviewbot.persist.UserRepository;
import ru.max.codereviewbot.service.PullRequestService;

@Component
public class ShowMyRequestsCommand extends BaseCommand {
    private static final Logger log = LoggerFactory.getLogger(ShowMyReviewsCommand.class);

    private final UserRepository userRepository;
    private final PullRequestService pullRequestService;

    @Autowired
    public ShowMyRequestsCommand(TamTamBotAPI maxApi, UserRepository userRepository, PullRequestService pullRequestService) {
        super(maxApi);
        this.userRepository = userRepository;
        this.pullRequestService = pullRequestService;
    }

    @Override
    public String getCommandDescription() {
        return "/show_my_requests - show your pull requests";
    }

    @Override
    public void execute(long userId, String[] args) {
        List<PullRequest> pullRequests = pullRequestService.findAllByAuthorId(userId);

        if (pullRequests.isEmpty()) {
            trySendMessage(userId, "You don't have any pull requests");
            return;
        }

        Set<Long> reviewerIds = pullRequests.stream().map(PullRequest::assignees).flatMap(List::stream).collect(Collectors.toSet());
        Map<Long, User> reviewers = userRepository.findAllByIds(reviewerIds).stream().collect(Collectors.toMap(User::id, user -> user));

        StringBuilder stringBuilder = new StringBuilder();
        for (PullRequest pullRequest : pullRequests) {
            StringBuilder reviewersBuilder = new StringBuilder();
            pullRequest.assignees().forEach(assign ->
                reviewersBuilder.append(
                    reviewers.containsKey(assign) ? "@" + reviewers.get(assign).username() + " " : ""
                )
            );

            stringBuilder
                .append("Review with id - ")
                .append(pullRequest.id())
                .append(". Status - ")
                .append(pullRequest.status().name())
                .append(". Assigned to ")
                .append(reviewersBuilder)
                .append("Url - ")
                .append(pullRequest.url())
                .append("\n");
        }
        trySendMessage(userId, stringBuilder.toString());
    }
}
