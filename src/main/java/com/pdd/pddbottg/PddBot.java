package com.pdd.pddbottg;

import com.pdd.pddbottg.handlers.CallbackHandler;
import com.pdd.pddbottg.handlers.RandomExamHandler;
import com.pdd.pddbottg.handlers.StartCommandHandler;
import com.pdd.pddbottg.handlers.UpdateHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class PddBot extends TelegramLongPollingBot {
    //private final UpdateHandler updateHandler;
    private final RandomExamHandler randomExamHandler;
    private final StartCommandHandler startCommandHandler;
    private final CallbackHandler callbackHandler;

    @Value("${telegram.bot.token}")
    private String botToken;
    @Value("${telegram.bot.username}")
    private String botUsername;

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
        randomExamHandler.handle(this, update);
        callbackHandler.handle(this, update);
    }




}
