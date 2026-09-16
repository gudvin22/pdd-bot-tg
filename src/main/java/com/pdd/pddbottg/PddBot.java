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
    private final GibddExamHandler gibddExamHandler;
    private final FeedbackHandler feedbackHandler;


    @Value("${telegram.bot.token}")
    private String botToken;
    @Value("${telegram.bot.username}")
    private String botUsername;

    @PostConstruct
    public void setCommands() {
        List<BotCommand> commands = List.of(
                //new BotCommand("start", "🏠 Перезапуск"),
                new BotCommand("refresh", "🔄 Перезапустить бота"),
                new BotCommand("help", "❓ Инструкция")
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
        if (startCommandHandler.handle(this, update)) return;
        if (refreshCommandHandler.handle(this, update)) return;
        if (helpCommandHandler.handle(this, update)) return;
        if (feedbackHandler.handle(this, update)) return;
        if (randomExamHandler.handle(this, update)) return;
        if (callbackHandler.handle(this, update)) return;
        if (errorViewHandler.handle(this, update)) return;
        if (ticketListHandler.handle(this, update)) return;
        if (statisticsHandler.handle(this, update)) return;
        if (recommendationHandler.handle(this, update)) return;
        if (gibddExamHandler.handle(this, update)) return;
    }




}
