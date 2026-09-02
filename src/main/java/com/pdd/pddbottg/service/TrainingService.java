package com.pdd.pddbottg.service;

import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.dto.RecommendationQuestionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingService {
    private final SessionStorage sessionStorage;
    private final MessageSender messageSender;

    public void handleAnswer(PddBot bot, Long chatId, int userAnswerIndex) {
        List<RecommendationQuestionDto> questions = sessionStorage.getTrainingQuestions(chatId);
        int currentIndex = sessionStorage.getTrainingIndex(chatId);

        if (questions == null || currentIndex >= questions.size()) {
            messageSender.sendMessage(bot, chatId, "⚠️ Ошибка. Попробуй начать заново.");
            return;
        }

        RecommendationQuestionDto question = questions.get(currentIndex);
        boolean isCorrect = userAnswerIndex == question.getCorrectAnswerIndex();

        if (isCorrect) {
            sessionStorage.incrementTrainingCorrectCount(chatId);
        }

        // Формируем сообщение с результатом
        StringBuilder result = new StringBuilder();
        result.append(isCorrect ? "✅ Правильно!" : "❌ Неправильно.").append("\n\n");

        List<String> answers = question.getAnswers();
        for (int i = 0; i < answers.size(); i++) {
            String prefix = "";
            if (i == question.getCorrectAnswerIndex()) {
                prefix = "✅ ";
            }
            if (i == userAnswerIndex && !isCorrect) {
                prefix = "❌ ";
            }
            result.append(prefix).append(i + 1).append(". ").append(answers.get(i)).append("\n");
        }

        if (question.getExplanation() != null && !question.getExplanation().isEmpty()) {
            result.append("\n📖 Объяснение:\n").append(question.getExplanation());
        }

           InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        // Кнопка "Дальше" (если не последний вопрос)
        if (currentIndex < questions.size() - 1) {
            InlineKeyboardButton nextBtn = new InlineKeyboardButton();
            nextBtn.setText("➡️ Дальше");
            nextBtn.setCallbackData("training_next");
            row.add(nextBtn);
        }

        // Кнопка "Завершить" (всегда)
        InlineKeyboardButton finishBtn = new InlineKeyboardButton();
        finishBtn.setText("🏁 Завершить");
        finishBtn.setCallbackData("training_finish");
        row.add(finishBtn);

        rows.add(row);
        keyboard.setKeyboard(rows);

        messageSender.sendMessageInlineKeyboard(bot, chatId, result.toString(), keyboard);

        // Увеличиваем индекс
        sessionStorage.incrementTrainingIndex(chatId);
    }

    public void showNextQuestion(PddBot bot, Long chatId) {
        List<RecommendationQuestionDto> questions = sessionStorage.getTrainingQuestions(chatId);
        int currentIndex = sessionStorage.getTrainingIndex(chatId);

        if (questions == null || currentIndex >= questions.size()) {
            messageSender.sendMessage(bot, chatId, "⚠️ Ошибка.");
            return;
        }

        RecommendationQuestionDto question = questions.get(currentIndex);

        if (question.getImageUrlSmall() != null && !question.getImageUrlSmall().isEmpty()) {
            try {
                messageSender.sendFoto(bot, chatId, question.getImageUrlSmall());
            } catch (Exception e) {
                // игнорируем
            }
        }

        messageSender.sendQuestionTextWithInline(
                bot,
                chatId,
                "📝 Вопрос " + (currentIndex + 1) + " из " + questions.size() + "\n\n" + question.getQuestionText(),
                question.getAnswers()
        );
    }

    public void showResult(PddBot bot, Long chatId) {
        List<RecommendationQuestionDto> questions = sessionStorage.getTrainingQuestions(chatId);
        int correctCount = sessionStorage.getTrainingCorrectCount(chatId);
        int total = questions != null ? questions.size() : 0;

        String message = "🏁 Тренировка завершена!\n\n" +
                "✅ Правильных ответов: " + correctCount + " из " + total + "\n" +
                "📊 Точность: " + (total > 0 ? (correctCount * 100) / total : 0) + "%";

        messageSender.sendMessage(bot, chatId, message);
        sessionStorage.removeTrainingSession(chatId);
    }
}