package ru.max.codereviewbot.domain;

import ru.max.codereviewbot.util.ImmutabilityUtils;

import java.util.List;

public record User(
        Long id,
        String name,
        String username) {
}
