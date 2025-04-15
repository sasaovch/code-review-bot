package ru.max.codereviewbot.domain;

import java.util.List;

public record PullRequest(
    long id,
    String url,
    PullRequestStatus status,
    long authorId,
    List<Long> assignees
) {
    public PullRequest updateStatus(PullRequestStatus status) {
        return new PullRequest(id, url, status, authorId, assignees);
    }
}
