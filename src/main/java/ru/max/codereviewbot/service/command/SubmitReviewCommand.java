package ru.max.codereviewbot.service.command;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import chat.tamtam.botapi.TamTamBotAPI;
import ru.max.codereviewbot.domain.PullRequest;
import ru.max.codereviewbot.domain.User;
import ru.max.codereviewbot.persist.UserRepository;
import ru.max.codereviewbot.service.PullRequestService;

import static ru.max.codereviewbot.domain.PullRequestStatus.WORK_IN_PROGRESS;

/*
Submit review command is used by reviewer to return PR to author for further improvement
 */
@Component
public class SubmitReviewCommand extends BaseCommand {

    private static final Logger log = LoggerFactory.getLogger(SubmitReviewCommand.class);

    private final UserRepository userRepository;
    private final PullRequestService pullRequestService;

    @Autowired
    public SubmitReviewCommand(TamTamBotAPI maxApi, UserRepository userRepository, PullRequestService pullRequestService) {
        super(maxApi);
        this.userRepository = userRepository;
        this.pullRequestService = pullRequestService;
    }

    @Override
    public String getCommandDescription() {
        return "/submit_review {url|id} - submit review comments";
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
            trySendMessage(userId, "You don't have permission to submit this pr");
            return;
        }

        pr = pr.updateStatus(WORK_IN_PROGRESS);
        pullRequestService.save(pr);

        User author = userRepository.get(pr.authorId()).orElseThrow();
        trySendMessage(author.id(), "Check out new comments for PR: " + pr.url());
    }
}
