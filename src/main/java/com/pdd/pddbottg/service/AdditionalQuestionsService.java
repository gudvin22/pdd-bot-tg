package com.pdd.pddbottg.service;

import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.dto.QuestionDto;
import com.pdd.pddbottg.entity.ExamSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdditionalQuestionsService {

    private final SessionStorage sessionStorage;
    private final MessageSender messageSender;
    private final KeyboardService keyboardService;

    /**
     * Запустить сессию дополнительных вопросов
     */
    public void start(PddBot bot, Long chatId, List<QuestionDto> questions) {
        ExamSession session = sessionStorage.getSession(chatId);
        if (session == null) return;

        session.setAdditionalQuestions(questions);
        session.setCurrentAdditionalIndex(0);
        session.setAdditionalAnswers(new ArrayList<>());

        // Показываем первый доп. вопрос
        showCurrent(bot, chatId);
    }

    /**
     * Обработать ответ пользователя на доп. вопрос
     */
    public void handleAnswer(PddBot bot, Long chatId, int userAnswerIndex) {
        ExamSession session = sessionStorage.getSession(chatId);
        if (session == null) return;

        int index = session.getCurrentAdditionalIndex();
        List<QuestionDto> questions = session.getAdditionalQuestions();

        if (questions == null || index >= questions.size()) return;

        QuestionDto question = questions.get(index);
        boolean isCorrect = userAnswerIndex == question.getCorrectAnswerIndex();

        if (!isCorrect) {
            // Ошибка в доп. вопросе → экзамен провален
            messageSender.sendMessage(bot, chatId, "❌ Ошибка в дополнительном вопросе! Экзамен не сдан.");
            messageSender.sendMessageWithReplyKeyboard(bot, chatId, "Главное меню", keyboardService.mainMenu());
            sessionStorage.removeSession(chatId);
            return;
        }

        // Правильно → идём дальше
        messageSender.sendMessage(bot, chatId, "✅ Правильно!");

        session.setCurrentAdditionalIndex(index + 1);
        showCurrent(bot, chatId);
    }

    /**
     * Показать текущий доп. вопрос
     */
    private void showCurrent(PddBot bot, Long chatId) {
        ExamSession session = sessionStorage.getSession(chatId);
        if (session == null) return;

        int index = session.getCurrentAdditionalIndex();
        List<QuestionDto> questions = session.getAdditionalQuestions();

        if (questions == null || index >= questions.size()) {
            // Все доп. вопросы пройдены → экзамен сдан
            messageSender.sendMessage(bot, chatId, "🎉 Все дополнительные вопросы пройдены! Экзамен сдан!");
            messageSender.sendMessageWithReplyKeyboard(bot, chatId, "Главное меню", keyboardService.mainMenu());
            sessionStorage.removeSession(chatId);
            return;
        }

        QuestionDto question = questions.get(index);

        messageSender.sendMessage(bot, chatId, "📝 Дополнительный вопрос " + (index + 1) + " из " + questions.size());

        if (question.getImageUrlSmall() != null && !question.getImageUrlSmall().isEmpty()) {
            try {
                messageSender.sendFoto(bot, chatId, question.getImageUrlSmall());
            } catch (Exception e) {
                // игнорируем
            }
        }

        messageSender.sendQuestionTextWithInline(bot, chatId, question.getQuestionText(), question.getAnswersText());
    }
}