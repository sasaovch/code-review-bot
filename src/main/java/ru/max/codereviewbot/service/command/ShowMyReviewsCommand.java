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
public class ShowMyReviewsCommand extends BaseCommand {
    private static final Logger log = LoggerFactory.getLogger(ShowMyReviewsCommand.class);

    private final UserRepository userRepository;
    private final PullRequestService pullRequestService;

    @Autowired
    public ShowMyReviewsCommand(TamTamBotAPI maxApi, UserRepository userRepository, PullRequestService pullRequestService) {
        super(maxApi);
        this.userRepository = userRepository;
        this.pullRequestService = pullRequestService;
    }

    @Override
    public String getCommandDescription() {
        return "/show_my_reviews - show PRs waiting for your review";
    }

    @Override
    public void execute(long userId, String[] args) {
        List<PullRequest> pullRequests = pullRequestService.findAllByAssignId(userId);

        if (pullRequests.isEmpty()) {
            trySendMessage(userId, "You don't have any reviews");
            return;
        }

        Set<Long> authorIds = pullRequests.stream().map(PullRequest::authorId).collect(Collectors.toSet());
        Map<Long, User> authors = userRepository.findAllByIds(authorIds).stream().collect(Collectors.toMap(User::id, user -> user));

        StringBuilder stringBuilder = new StringBuilder();
        for (PullRequest pullRequest : pullRequests) {
            User author = authors.get(pullRequest.authorId());

            stringBuilder
                .append("Review with id - ")
                .append(pullRequest.id())
                .append(". Status - ")
                .append(pullRequest.status().name())
                .append(". Author is ")
                .append(author.name())
                .append(" @")
                .append(author.username())
                .append(" with userId ")
                .append(author.id())
                .append(". Url - ")
                .append(pullRequest.url())
                .append("\n");
        }
        trySendMessage(userId, stringBuilder.toString());
    }
}
