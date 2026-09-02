package com.pdd.pddbottg;

import com.pdd.pddbottg.handlers.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PddBot extends TelegramLongPollingBot {
    //private final UpdateHandler updateHandler;
    private final RandomExamHandler randomExamHandler;
    private final StartCommandHandler startCommandHandler;
    private final CallbackHandler callbackHandler;
    private final ErrorViewHandler errorViewHandler;
    private final HelpCommandHandler helpCommandHandler;
    private final TicketListHandler ticketListHandler;
    private final StatisticsHandler statisticsHandler;
    private final RefreshCommandHandler refreshCommandHandler;
    private final RecommendationHandler recommendationHandler;


    @Value("${telegram.bot.token}")
    private String botToken;
    @Value("${telegram.bot.username}")
    private String botUsername;

    @PostConstruct
    public void setCommands() {
        List<BotCommand> commands = List.of(
                new BotCommand("start", "🏠 Перезапуск"),
                new BotCommand("refresh", "🔄 Перезапустить бота"),
                new BotCommand("help", "❓ Помощь")
        );
        try {
            SetMyCommands setMyCommands = SetMyCommands.builder()
                    .commands(commands)
                    .build();
            execute(setMyCommands);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }


    @Override
    public void onUpdateReceived(Update update) {
        startCommandHandler.handle(this, update);
        refreshCommandHandler.handle(this, update);
        helpCommandHandler.handle(this, update);
        randomExamHandler.handle(this, update);
        callbackHandler.handle(this, update);
        errorViewHandler.handle(this, update);
        ticketListHandler.handle(this, update);
        statisticsHandler.handle(this, update);
        recommendationHandler.handle(this, update);
    }




}
