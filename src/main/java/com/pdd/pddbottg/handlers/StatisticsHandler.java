package com.pdd.pddbottg.handlers;

import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.dto.UserStatisticsDto;
import com.pdd.pddbottg.dto.WeakTopicDto;
import com.pdd.pddbottg.service.MessageSender;
import com.pdd.pddbottg.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StatisticsHandler implements UpdateHandler {
    private final StatisticsService statisticsService;
    private final MessageSender messageSender;

    @Override
    public boolean handle(PddBot bot, Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String telegramId = String.valueOf(update.getMessage().getFrom().getId());
            String userName = update.getMessage().getFrom().getFirstName();

            if ("📊 Статистика".equals(text)) {
                try {
                    UserStatisticsDto stats = statisticsService.getUserStatistics(telegramId, userName);

                    StringBuilder sb = new StringBuilder();
                    sb.append("📊 <b>Твоя статистика</b>\n\n");

                    // Общие метрики
                    sb.append("📝 Всего попыток: <b>").append(stats.getTotalAttempts()).append("</b>\n");

                    // Прогресс по билетам
                    int totalTickets = 40;
                    int progressPercent = (stats.getCorrectTickets() * 100) / totalTickets;
                    sb.append("📈 Прогресс: <b>").append(stats.getCorrectTickets())
                            .append("</b> из ").append(totalTickets)
                            .append(" билетов (<b>").append(progressPercent).append("%</b>)\n");

                    // Точность ответов
                    int totalAnswers = stats.getTotalAttempts() * 20;
                    int correctAnswers = totalAnswers - stats.getTotalWrong();
                    int accuracyPercent = (correctAnswers * 100) / totalAnswers;
                    sb.append("🎯 Точность: <b>").append(accuracyPercent).append("%</b> правильных ответов\n\n");

                    // Статусы билетов
                    sb.append("✅ Билетов без ошибок: <b>").append(stats.getCorrectTickets()).append("</b>\n");
                    sb.append("❌ Билетов с ошибками: <b>").append(stats.getIncorrectTickets()).append("</b>\n");

                    // Дата последней попытки
                    if (stats.getLastAttemptDate() != null) {
                        String formattedDate = stats.getLastAttemptDate()
                                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                        sb.append("🕐 Последняя попытка: <b>").append(formattedDate).append("</b>\n\n");
                    }

                    // Слабые темы
                    if (stats.getWeakTopics() != null && !stats.getWeakTopics().isEmpty()) {
                        sb.append("🔴 <b>Слабые темы (топ-5):</b>\n");
                        for (int i = 0; i < stats.getWeakTopics().size(); i++) {
                            WeakTopicDto topic = stats.getWeakTopics().get(i);
                            sb.append("  ").append(i + 1).append(". ")
                                    .append(topic.getTopicName())
                                    .append(" — <b>").append(topic.getWrongCount())
                                    .append("</b> ошибок\n");
                        }
                    }

                    // Инлайн-кнопка для AI-анализа
                    InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                    List<InlineKeyboardButton> row = new ArrayList<>();

                    InlineKeyboardButton aiButton = new InlineKeyboardButton();
                    aiButton.setText("🧠 Получить AI-анализ");
                    aiButton.setCallbackData("ai_statistics_analysis");
                    row.add(aiButton);
                    rows.add(row);
                    keyboard.setKeyboard(rows);

                    messageSender.sendMessageInlineKeyboard(bot, chatId, sb.toString(), keyboard);

                } catch (Exception e) {
                    messageSender.sendMessage(bot, chatId, "❌ Ошибка получения статистики: " + e.getMessage());
                }
                return true;
            }
        }
        return false;
    }
}