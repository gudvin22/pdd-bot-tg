package com.pdd.pddbottg.service;

import com.pdd.pddbottg.PddBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

    public void sendMessageWithReplyKeyboard(PddBot bot, Long chatId, String text, ReplyKeyboardMarkup keyboard) {

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

    public void sendMessageInlineKeyboard(PddBot bot, Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboard);
        message.setParseMode(ParseMode.HTML);
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

    public void sendQuestionTextWithInline(PddBot bot, Long chatId, String questionText, List<String> answers) {
        SendMessage message = new SendMessage();
        StringBuilder textBuilder = new StringBuilder();
        message.setChatId(chatId.toString());
        message.setParseMode("HTML");
        textBuilder.append("<b>").append(questionText).append("</b>");
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = 0; i < answers.size(); i++) {
            textBuilder.append("\n").append(i + 1).append(". ").append(answers.get(i));
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.valueOf((i + 1)));
            button.setCallbackData("answer_" + i);
            row.add(button);
        }
        message.setText(textBuilder.toString());
        rows.add(row);
        keyboard.setKeyboard(rows);
        message.setReplyMarkup(keyboard);
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public Integer sendMessageInlineKeyboardAndReturnId(PddBot bot, Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboard);
        message.setParseMode(ParseMode.HTML);
        try {
            var sent = bot.execute(message);
            return sent.getMessageId();
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void editMessage(PddBot bot, Long chatId, Integer messageId, String newText) {
        EditMessageText edit = new EditMessageText();
        edit.setChatId(chatId.toString());
        edit.setMessageId(messageId);
        edit.setText(newText);
        edit.setParseMode(ParseMode.HTML);
        try {
            bot.execute(edit);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendPhoto(PddBot bot, Long chatId, String fileId, String caption) {
        sendPhotoAndReturnId(bot, chatId, fileId, caption, null);
    }

    /**
     * Отправить фото по fileId с подписью и клавиатурой. Возвращает messageId.
     */
    public Integer sendPhotoAndReturnId(PddBot bot, Long chatId, String fileId,
                                        String caption, InlineKeyboardMarkup keyboard) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId.toString());
        photo.setPhoto(new InputFile(fileId));
        if (caption != null && !caption.isEmpty()) {
            photo.setCaption(caption);
            photo.setParseMode(ParseMode.HTML);
        }
        if (keyboard != null) {
            photo.setReplyMarkup(keyboard);
        }
        try {
            var sent = bot.execute(photo);
            return sent.getMessageId();
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void editCaption(PddBot bot, Long chatId, Integer messageId, String newCaption) {
        EditMessageCaption edit = new EditMessageCaption();
        edit.setChatId(chatId.toString());
        edit.setMessageId(messageId);
        edit.setCaption(newCaption);
        edit.setParseMode(ParseMode.HTML);
        try {
            bot.execute(edit);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }






}