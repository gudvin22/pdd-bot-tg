package com.pdd.pddbottg.service;

import com.pdd.pddbottg.SessionStorage;
import com.pdd.pddbottg.dto.RecommendationQuestionDto;
import com.pdd.pddbottg.entity.ExamSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SessionStorageTest {

    private SessionStorage sessionStorage;
    private final Long chatId = 123L;
    private final Long adminChatId = 999L;

    @BeforeEach
    void setUp() {
        // Создаем чистый объект перед каждым тестом
        sessionStorage = new SessionStorage();
    }

    // ========== Тесты для обычной сессии (Экзамен/Билет) ==========

    @Test
    void putAndGetSession_shouldWorkCorrectly() {
        // Для теста используем мок-объект, так как нам важна только ссылка
        ExamSession session = new ExamSession(1, List.of(), false);

        sessionStorage.putSession(chatId, session);

        assertEquals(session, sessionStorage.getSession(chatId));
    }

    @Test
    void removeSession_shouldClearAllUserData() {
        // 1. Заполняем данными
        sessionStorage.putSession(chatId, new ExamSession(1, List.of(), false));
        sessionStorage.putTrainingQuestions(chatId, List.of(new RecommendationQuestionDto()));
        sessionStorage.setFeedbackMode(chatId);

        // 2. Действие
        sessionStorage.removeSession(chatId);

        // 3. Проверка, что всё очистилось
        assertNull(sessionStorage.getSession(chatId));
        assertFalse(sessionStorage.isTrainingMode(chatId));
        assertFalse(sessionStorage.isFeedbackMode(chatId));
    }

    // ========== Тесты для тренировки (Рекомендации) ==========

    @Test
    void trainingMode_shouldInitializeAndIncrementCorrectly() {
        List<RecommendationQuestionDto> questions = List.of(new RecommendationQuestionDto(), new RecommendationQuestionDto());

        sessionStorage.putTrainingQuestions(chatId, questions);

        assertTrue(sessionStorage.isTrainingMode(chatId));
        assertEquals(2, sessionStorage.getTrainingQuestions(chatId).size());
        assertEquals(0, sessionStorage.getTrainingIndex(chatId));
        assertEquals(0, sessionStorage.getTrainingCorrectCount(chatId));

        // Имитируем ответы пользователя
        sessionStorage.incrementTrainingIndex(chatId);
        sessionStorage.incrementTrainingCorrectCount(chatId);

        assertEquals(1, sessionStorage.getTrainingIndex(chatId));
        assertEquals(1, sessionStorage.getTrainingCorrectCount(chatId));
    }

    @Test
    void removeTrainingSession_shouldClearTrainingData() {
        sessionStorage.putTrainingQuestions(chatId, List.of());
        sessionStorage.removeTrainingSession(chatId);

        assertFalse(sessionStorage.isTrainingMode(chatId));
        assertNull(sessionStorage.getTrainingQuestions(chatId));
        assertEquals(0, sessionStorage.getTrainingIndex(chatId)); // Возвращает default 0
    }

    // ========== Тесты для обратной связи ==========

    @Test
    void feedbackMode_shouldToggleCorrectly() {
        assertFalse(sessionStorage.isFeedbackMode(chatId));

        sessionStorage.setFeedbackMode(chatId);
        assertTrue(sessionStorage.isFeedbackMode(chatId));

        sessionStorage.removeFeedbackMode(chatId);
        assertFalse(sessionStorage.isFeedbackMode(chatId));
    }

    // ========== Тесты для ответов Админа ==========

    @Test
    void adminReply_shouldSaveAndClearTargetUser() {
        Long userId = 456L;

        sessionStorage.setAdminReplyTo(adminChatId, userId);
        assertTrue(sessionStorage.isAdminReplying(adminChatId));
        assertEquals(userId, sessionStorage.getAdminReplyTo(adminChatId));

        sessionStorage.clearAdminReplyTo(adminChatId);
        assertFalse(sessionStorage.isAdminReplying(adminChatId));
        assertNull(sessionStorage.getAdminReplyTo(adminChatId));
    }

    @Test
    void adminMessageIdAndText_shouldSaveAndClear() {
        Integer msgId = 777;
        String text = "Текст сообщения";

        sessionStorage.setAdminMessageId(adminChatId, msgId);
        sessionStorage.setAdminOriginalText(adminChatId, text);

        assertEquals(msgId, sessionStorage.getAdminMessageId(adminChatId));
        assertEquals(text, sessionStorage.getAdminOriginalText(adminChatId));

        sessionStorage.clearAdminMessageId(adminChatId);
        sessionStorage.clearAdminOriginalText(adminChatId);

        assertNull(sessionStorage.getAdminMessageId(adminChatId));
        assertNull(sessionStorage.getAdminOriginalText(adminChatId));
    }
}
