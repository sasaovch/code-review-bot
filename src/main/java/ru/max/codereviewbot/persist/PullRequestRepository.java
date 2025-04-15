package ru.max.codereviewbot.persist;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Repository;

import ru.max.codereviewbot.domain.PullRequest;

@Repository
public class PullRequestRepository {

    private static final Random random = new Random();

    private final Map<Long, PullRequest> pullRequests = new ConcurrentHashMap<>();

    public void save(@NotNull PullRequest pullRequest) {
        pullRequests.put(pullRequest.id(), pullRequest);
    }

    public static long nextId() {
        long time = System.currentTimeMillis();
        return time + random.nextLong(Long.MAX_VALUE - time);
    }

    public Optional<PullRequest> findByUrl(@NotNull String url) {
        for (PullRequest pullRequest : pullRequests.values()) {
            if (pullRequest.url().equals(url)) {
                return Optional.of(pullRequest);
            }
        }
        return Optional.empty();
    }

    public Optional<PullRequest> findById(@NotNull Long id) {
        if (pullRequests.containsKey(id)) {
            return Optional.of(pullRequests.get(id));
        }
        return Optional.empty();
    }

    public List<PullRequest> findAllByAuthorId(long userId) {
        return pullRequests
            .values()
            .stream()
            .filter(pr -> pr.authorId() == userId)
            .toList();
    }

    public List<PullRequest> findAllByAssignId(long userId) {
        return pullRequests
            .values()
            .stream()
            .filter(pr -> pr.assignees().contains(userId))
            .toList();
    }
}
