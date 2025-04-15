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
public class AssignReviewerCommand extends BaseCommand {
    private static final Logger log = LoggerFactory.getLogger(AssignReviewerCommand.class);

    private final UserRepository userRepository;
    private final PullRequestService pullRequestService;

    @Autowired
    public AssignReviewerCommand(TamTamBotAPI maxApi, UserRepository userRepository, PullRequestService pullRequestService) {
        super(maxApi);
        this.userRepository = userRepository;
        this.pullRequestService = pullRequestService;
    }

    @Override
    public void execute(long userId, String[] args) {
        Optional<String> validationResult = validateArgs(args);
        if (validationResult.isPresent()) {
            trySendMessage(userId, validationResult.get());
            return;
        }

        PullRequest pr = pullRequestService.findByIdOrUrl(args[0]);
        if (pr == null) {
            trySendMessage(userId, "PR with specified url|id does not exist");
            return;
        }

        if (pr.authorId() != userId) {
            trySendMessage(userId, "You are not allowed to assign this PR");
            return;
        }

        Optional<User> reviewerOpt = userRepository.get(Long.parseLong(args[1]));
        if (reviewerOpt.isEmpty()) {
            trySendMessage(userId, "User with userId " + args[1] + " does not exist");
            return;
        }

        User user = reviewerOpt.get();
        pr.assignees().add(user.id());
        pullRequestService.save(pr);

        trySendMessage(user.id(), "You were assigned to pr" + pr.url());
        trySendMessage(userId, "Done");
    }

    private Optional<String> validateArgs(String[] args) {
        Optional<String> validationArgs = shouldBeTwoArg(args);
        if (validationArgs.isPresent()) {
            return validationArgs;
        }

        if (!(args[0].chars().allMatch(Character::isDigit) || shouldBeValidUrl(args[0]).isEmpty())) {
            return Optional.of("Incorrect the first argument for command");
        }

        if (args[1].chars().allMatch(Character::isDigit)) {
            return Optional.empty();
        }
        return Optional.of("Incorrect the second argument for command");
    }

    @Override
    public String getCommandDescription() {
        return "/assign {url|id} userId - assign review to user";
    }
}
