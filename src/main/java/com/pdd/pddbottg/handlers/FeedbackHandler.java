package com.pdd.pddbottg.handlers;

import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.service.KeyboardService;
import com.pdd.pddbottg.service.MessageSender;
import com.pdd.pddbottg.service.SessionStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedbackHandler implements UpdateHandler {

    private final MessageSender messageSender;
    private final SessionStorage sessionStorage;
    private final KeyboardService keyboardService;

    @Value("${bot.admin.id}")
    private String adminIdString;

    private Long adminId() {
        return Long.parseLong(adminIdString);
    }

    @Override
    public boolean handle(PddBot bot, Update update) {

        // ============ 1. Обработка callback «Ответить» ============
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long adminChatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.startsWith("reply_to_")) {
                Long userId = Long.parseLong(callbackData.substring("reply_to_".length()));

                sessionStorage.setAdminReplyTo(adminChatId, userId);
                sessionStorage.setAdminMessageId(adminChatId, update.getCallbackQuery().getMessage().getMessageId());

                messageSender.sendMessage(bot, adminChatId,
                        "✏️ Напишите ответ для пользователя ID " + userId);

                return true;
            }
        }

        // ============ 2. Обработка сообщений (текст и/или фото) ============
        if (update.hasMessage() && (update.getMessage().hasText() || update.getMessage().hasPhoto())) {
            String text = update.getMessage().getText() != null
                    ? update.getMessage().getText()
                    : update.getMessage().getCaption();   // если фото с подписью
            Long chatId = update.getMessage().getChatId();
            String firstName = update.getMessage().getFrom().getFirstName();
            String telegramIdStr = String.valueOf(update.getMessage().getFrom().getId());
            Long telegramId = Long.parseLong(telegramIdStr);
            String fileId = getPhotoFileId(update);

            // ---------- 2.1. Ответ админа пользователю ----------
            if (chatId.equals(adminId()) && sessionStorage.isAdminReplying(chatId)) {
                Long userId = sessionStorage.getAdminReplyTo(chatId);
                if (userId == null) {
                    sessionStorage.clearAdminReplyTo(chatId);
                    return false;
                }

                String caption = "📩 Ответ от администратора:" +
                        (text != null && !text.isEmpty() ? "\n" + text : "");

                // отправляем пользователю фото или текст
                if (fileId != null) {
                    messageSender.sendPhoto(bot, userId, fileId, caption);
                } else {
                    messageSender.sendMessage(bot, userId, caption);
                }

                // редактируем сообщение админа: убираем кнопку, ставим «Отвечено»
                Integer msgId = sessionStorage.getAdminMessageId(chatId);
                String originalText = sessionStorage.getAdminOriginalText(chatId);
                if (msgId != null) {
                    String doneText = (originalText != null ? originalText : "") +
                            "\n\n✅ Отвечено" + (text != null && !text.isEmpty() ? ": " + text : "");

                    // ★ Определяем: сообщение с фото или без
                    if (fileId != null) {
                        // сообщение с фото → обновляем подпись
                        messageSender.editCaption(bot, chatId, msgId, doneText);
                    } else {
                        // обычное текстовое сообщение → обновляем текст
                        messageSender.editMessage(bot, chatId, msgId, doneText);
                    }
                }

                sessionStorage.clearAdminReplyTo(chatId);
                sessionStorage.clearAdminMessageId(chatId);
                sessionStorage.clearAdminOriginalText(chatId);

                return true;
            }

            // ---------- 2.2. Пользователь нажал «💬 Обратная связь» ----------
            if ("💬 Обратная связь".equals(text)) {
                sessionStorage.setFeedbackMode(chatId);
                messageSender.sendMessage(bot, chatId,
                        "✍️ Напишите сообщение администратору:\n" +
                                "📄 текст  или  🖼️ текст + картинка\n\n" +
                                "Отменить: /cancel");
                return true;
            }

            // ---------- 2.3. Отмена ----------
            if (sessionStorage.isFeedbackMode(chatId) && "/cancel".equals(text)) {
                sessionStorage.removeFeedbackMode(chatId);
                messageSender.sendMessage(bot, chatId, "Отменено.");
                return true;
            }

            // ---------- 2.4. Пользователь пишет фидбек ----------
            if (sessionStorage.isFeedbackMode(chatId)) {
                sessionStorage.removeFeedbackMode(chatId);

                // формируем текст для админа
                String adminText = "📩 Новое сообщение от пользователя:\n" +
                        "Имя: " + firstName + "\n" +
                        "ID: " + telegramId + "\n" +
                        (text != null && !text.isEmpty() ? "\n" + text : "");

                InlineKeyboardMarkup keyboard = buildReplyKeyboard(telegramId);

                Integer messageId;
                if (fileId != null) {
                    // отправляем админу фото с подписью и кнопкой
                    messageId = messageSender.sendPhotoAndReturnId(
                            bot, adminId(), fileId, adminText, keyboard);
                } else {
                    // отправляем админу только текст
                    messageId = messageSender.sendMessageInlineKeyboardAndReturnId(
                            bot, adminId(), adminText, keyboard);
                }

                if (messageId != null) {
                    sessionStorage.setAdminMessageId(adminId(), messageId);
                    sessionStorage.setAdminOriginalText(adminId(), adminText);
                }

                messageSender.sendMessage(bot, chatId,
                        "✅ Сообщение отправлено! Я отвечу вам в ближайшее время.");

                return true;
            }
        }

        return false;
    }

    /**
     * Возвращает fileId самой большой версии фото (или null, если фото нет)
     */
    private String getPhotoFileId(Update update) {
        if (!update.getMessage().hasPhoto()) return null;
        List<org.telegram.telegrambots.meta.api.objects.PhotoSize> photos = update.getMessage().getPhoto();
        return photos.get(photos.size() - 1).getFileId();
    }

    private InlineKeyboardMarkup buildReplyKeyboard(Long userId) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton replyBtn = new InlineKeyboardButton();
        replyBtn.setText("✉️ Ответить");
        replyBtn.setCallbackData("reply_to_" + userId);
        row.add(replyBtn);
        rows.add(row);

        keyboard.setKeyboard(rows);
        return keyboard;
    }
}