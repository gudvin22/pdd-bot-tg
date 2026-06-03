package com.pdd.pddbottg;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
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

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
