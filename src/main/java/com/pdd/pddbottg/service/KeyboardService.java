package com.pdd.pddbottg.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class KeyboardService {

    public ReplyKeyboardMarkup mainMenu() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false); // постоянное

        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("🎯 Рекомендация");
        row1.add("🎲 Случайный билет");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("📝 Режим экзамена");
        row2.add("📚 Все билеты");


        KeyboardRow row3 = new KeyboardRow();
        row3.add("📊 Статистика");
        row3.add("⭐ Подписка");

        KeyboardRow row4 = new KeyboardRow();

        row4.add("💬 Помощь и обратная связь");

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        rows.add(row4);
        replyKeyboardMarkup.setKeyboard(rows);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup mainRandomMenu() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);

        replyKeyboardMarkup.setOneTimeKeyboard(true);
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("🎲 Случайный билет");
        replyKeyboardMarkup.setKeyboard(Collections.singletonList(keyboardRow));

        return replyKeyboardMarkup;
    }

    public InlineKeyboardMarkup viewErrorsKeyboard() {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("📘 Посмотреть ошибки");
        button.setCallbackData("view_errors");
        rows.add(Collections.singletonList(button));
        keyboard.setKeyboard(rows);
        return keyboard;
    }
}
