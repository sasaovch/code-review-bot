package ru.max.codereviewbot.service;

import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Nullable;
import ru.max.codereviewbot.domain.PullRequest;
import ru.max.codereviewbot.persist.PullRequestRepository;

@Service
public class PullRequestService {
    private final PullRequestRepository pullRequestRepository;

    @Autowired
    public PullRequestService(PullRequestRepository pullRequestRepository) {
        this.pullRequestRepository = pullRequestRepository;
    }

    public void save(@NotNull PullRequest pr) {
        pullRequestRepository.save(pr);
    }

    @Nullable
    public PullRequest findByIdOrUrl(@Nullable String id) {
        if (id == null) {
            return null;
        }
        boolean isNumeric = id.chars().allMatch(Character::isDigit);
        Optional<PullRequest> opr;
        if (isNumeric) {
            opr = pullRequestRepository.findById(Long.parseLong(id));
        } else {
            opr = pullRequestRepository.findByUrl(id);
        }

        return opr.orElse(null);
    }

    @NotNull
    public List<PullRequest> findAllByAuthorId(long userId) {
        return pullRequestRepository.findAllByAuthorId(userId);
    }

    @NotNull
    public List<PullRequest> findAllByAssignId(long userId) {
        return pullRequestRepository.findAllByAssignId(userId);
    }
}
