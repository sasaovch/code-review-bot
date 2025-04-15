package ru.max.codereviewbot.testutil;

import ru.max.codereviewbot.domain.User;

import java.util.Random;

public class RandomEntity {
    private static final Random RANDOM = new Random();

    public static User randomUser() {
        return new User(
                RANDOM.nextLong(),
                RandomString.generateRandomString(10),
                RandomString.generateRandomString(10)
        );
    }
}
