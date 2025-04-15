package ru.max.codereviewbot.domain;

public enum PullRequestStatus {
    WORK_IN_PROGRESS,
    WAITING_FOR_REVIEW,
    WAITING_FOR_APPROVAL,
    READY_TO_BE_MERGED,
    MERGED
}
