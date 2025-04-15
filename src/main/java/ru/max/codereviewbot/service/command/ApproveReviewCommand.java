package ru.max.codereviewbot.service.command;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import chat.tamtam.botapi.TamTamBotAPI;
import ru.max.codereviewbot.domain.PullRequest;
import ru.max.codereviewbot.domain.PullRequestStatus;
import ru.max.codereviewbot.domain.User;
import ru.max.codereviewbot.persist.UserRepository;
import ru.max.codereviewbot.service.PullRequestService;

@Component
public class ApproveReviewCommand extends BaseCommand {
    private static final Logger log = LoggerFactory.getLogger(ApproveReviewCommand.class);

    private final UserRepository userRepository;
    private final PullRequestService pullRequestService;

    @Autowired
    public ApproveReviewCommand(TamTamBotAPI maxApi, UserRepository userRepository, PullRequestService pullRequestService) {
        super(maxApi);
        this.userRepository = userRepository;
        this.pullRequestService = pullRequestService;
    }

    @Override
    public String getCommandDescription() {
        return "/approve {url|id} - approve PR";
    }

    @Override
    public void execute(long userId, String[] args) {
        Optional<String> validationResult = validateArgsAsUrlOrId(args);
        if (validationResult.isPresent()) {
            trySendMessage(userId, validationResult.get());
            return;
        }

        PullRequest pr = pullRequestService.findByIdOrUrl(args[0]);
        if (pr == null) {
            trySendMessage(userId, "PR with specified url|id does not exist");
            return;
        }

        if (!pr.assignees().contains(userId)) {
            trySendMessage(userId, "You are not allowed to approve this PR");
            return;
        }

        pr = pr.updateStatus(PullRequestStatus.READY_TO_BE_MERGED);
        pullRequestService.save(pr);

        trySendMessage(userId, "PR approved");

        Optional<User> reviewer = userRepository.get(userId);
        if (reviewer.isEmpty()) {
            log.warn("User with id {} not found", userId);
            trySendMessage(pr.authorId(), "PR approved\n" + pr.url());
            return;
        }
        trySendMessage(pr.authorId(), "PR approved by @" + reviewer.get().username() + "\n" + pr.url());
    }
}
