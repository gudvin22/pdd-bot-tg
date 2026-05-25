package com.pdd.pddbottg;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class PddBot extends TelegramLongPollingBot {

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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText(); // текст сообщения
            Long chatId = update.getMessage().getChatId(); // уникальный идентификатор чата с пользователем

            if ("/start".equals(text)) { // если пользователь отправил команду /start
                // отправляем приветственное сообщение
                sendMessage(chatId, "Привет! Я бот для подготовки к экзамену ПДД.");
            } else {
                // на любое другое сообщение отвечаем, что не знаем команду
                sendMessage(chatId, "Я понимаю только команду /start.");
            }
        }

    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();          // создаём объект сообщения
        message.setChatId(chatId.toString());            // указываем получателя
        message.setText(text);                           // пишем текст
        try {
            execute(message);                            // отправляем через Telegram API
        } catch (TelegramApiException e) {
            e.printStackTrace();                         // если ошибка, печатаем её в консоль
        }
    }


}
