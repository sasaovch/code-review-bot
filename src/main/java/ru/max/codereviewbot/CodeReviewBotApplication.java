package ru.max.codereviewbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import chat.tamtam.botapi.TamTamBotAPI;
import org.springframework.context.annotation.Bean;
import ru.max.codereviewbot.config.BotConfig;

@SpringBootApplication
public class CodeReviewBotApplication {

    @Bean
    public TamTamBotAPI tamTamBotAPI(BotConfig botConfig) {
        return TamTamBotAPI.create(botConfig.getToken());
    }

    public static void main(String[] args) {
        SpringApplication.run(CodeReviewBotApplication.class, args);
    }
}
