package com.pdd.pddbottg.service;

import com.pdd.pddbottg.PddBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageSender {
    @Value("${myserver.address}")
    private String serverAddress;

    public void sendMessage(PddBot bot, Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageWithKeyboard(PddBot bot, Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboard);
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendQuestionInline(PddBot bot, Long chatId, String questionText, List<String> answers) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(questionText);
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < answers.size(); i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(answers.get(i));
            button.setCallbackData("answer_" + i);
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }
        keyboard.setKeyboard(rows);
        message.setReplyMarkup(keyboard);
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendFoto(PddBot bot, Long chatId, String imageUrl) throws IOException {

        String fullImageUrl = serverAddress + imageUrl;
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId.toString());

        //отправка файла напрямую пока localhost для тестов,
        // при деплое удалить вместе с папкой static.images-bilety
        if(serverAddress.contains("localhost")){
            String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            org.springframework.core.io.Resource resource = new org.springframework.core.io.ClassPathResource("static/images-bilety/" + fileName);
            InputFile inputFile = new InputFile(resource.getInputStream(), fileName);
            sendPhoto.setPhoto(inputFile);
        }   else  {
            //------------------------------------------------------
            sendPhoto.setPhoto(new InputFile(fullImageUrl));
        }

        try {
            bot.execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}