package ru.max.codereviewbot.testutil;

import java.util.Random;

public class RandomString {

    private static final Random RANDOM = new Random();

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int choice = RANDOM.nextInt(3);

            char randomChar = switch (choice) {
                case 0 -> // Uppercase letter (ASCII 65-90)
                        (char) (RANDOM.nextInt(26) + 'A');
                case 1 -> // Lowercase letter (ASCII 97-122)
                        (char) (RANDOM.nextInt(26) + 'a');
                case 2 -> // Digit (ASCII 48-57)
                        (char) (RANDOM.nextInt(10) + '0');
                default -> throw new IllegalStateException("Unexpected choice: " + choice);
            };

            sb.append(randomChar);
        }

        return sb.toString();
    }
}
