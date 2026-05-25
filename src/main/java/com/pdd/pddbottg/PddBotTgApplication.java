package com.pdd.pddbottg;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class PddBotTgApplication {

    public static void main(String[] args) {
        SpringApplication.run(PddBotTgApplication.class, args);
    }

    @Bean
    public CommandLineRunner registerBot(PddBot bot) {
        return args -> {
            new TelegramBotsApi(DefaultBotSession.class).registerBot(bot);
            System.out.println("Бот запущен");
        };
    }
}
