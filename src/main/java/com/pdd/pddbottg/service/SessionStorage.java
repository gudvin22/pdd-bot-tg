package com.pdd.pddbottg.service;

import com.pdd.pddbottg.dto.RecommendationQuestionDto;
import com.pdd.pddbottg.entity.ExamSession;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionStorage {

    private final Map<Long, ExamSession> sessions = new ConcurrentHashMap<>();
    private final Map<Long, List<RecommendationQuestionDto>> trainingSessions = new ConcurrentHashMap<>();
    private final Map<Long, Integer> trainingIndexes = new ConcurrentHashMap<>();
    private final Map<Long, Integer> trainingCorrectCount = new ConcurrentHashMap<>();

    /** Пользователи, которые сейчас пишут в обратную связь */
    private final Set<Long> feedbackMode = ConcurrentHashMap.newKeySet();
    /** Какому пользователю сейчас отвечает админ: adminChatId → userId */
    private final Map<Long, Long> adminReplyTo = new ConcurrentHashMap<>();
    /** ID сообщения админа, которое надо отредактировать: adminChatId → messageId */
    private final Map<Long, Integer> adminMessageId = new ConcurrentHashMap<>();
    /** Текст исходного сообщения админа (чтобы после редактирования не потерять контекст) */
    private final Map<Long, String> adminOriginalText = new ConcurrentHashMap<>();


    public void putSession(Long chatId, ExamSession examSession) {
        sessions.put(chatId, examSession);
    }

    public ExamSession getSession(Long chatId) {
        return sessions.get(chatId);
    }

    public void removeSession(Long chatId) {
        sessions.remove(chatId);
        trainingSessions.remove(chatId);
        trainingIndexes.remove(chatId);
        trainingCorrectCount.remove(chatId);
        feedbackMode.remove(chatId);
    }

    // === Методы для тренировки (рекомендации) ===
    public void putTrainingQuestions(Long chatId, List<RecommendationQuestionDto> questions) {
        trainingSessions.put(chatId, questions);
        trainingIndexes.put(chatId, 0);
        trainingCorrectCount.put(chatId, 0);
    }

    public List<RecommendationQuestionDto> getTrainingQuestions(Long chatId) {
        return trainingSessions.get(chatId);
    }

    public int getTrainingIndex(Long chatId) {
        return trainingIndexes.getOrDefault(chatId, 0);
    }

    public void incrementTrainingIndex(Long chatId) {
        trainingIndexes.put(chatId, trainingIndexes.getOrDefault(chatId, 0) + 1);
    }

    public void removeTrainingSession(Long chatId) {
        trainingSessions.remove(chatId);
        trainingIndexes.remove(chatId);
        trainingCorrectCount.remove(chatId);
    }

    public boolean isTrainingMode(Long chatId) {
        return trainingSessions.containsKey(chatId);
    }

    public void incrementTrainingCorrectCount(Long chatId) {
        trainingCorrectCount.put(chatId, trainingCorrectCount.getOrDefault(chatId, 0) + 1);
    }

    public int getTrainingCorrectCount(Long chatId) {
        return trainingCorrectCount.getOrDefault(chatId, 0);
    }

    // === методы для обратной связи ===
    //пользователь пишет фидбек
    public void setFeedbackMode(Long chatId) {
        feedbackMode.add(chatId);
    }

    public boolean isFeedbackMode(Long chatId) {
        return feedbackMode.contains(chatId);
    }

    public void removeFeedbackMode(Long chatId) {
        feedbackMode.remove(chatId);
    }

    //Админ начал отвечать пользователю
    public void setAdminReplyTo(Long adminChatId, Long userId) {
        adminReplyTo.put(adminChatId, userId);
    }

    // кому отвечает админ
    public Long getAdminReplyTo(Long adminChatId) {
        return adminReplyTo.get(adminChatId);
    }

    //Админ завершил ответ
    public void clearAdminReplyTo(Long adminChatId) {
        adminReplyTo.remove(adminChatId);
    }

    //отвечает ли админ кому-то
    public boolean isAdminReplying(Long adminChatId) {
        return adminReplyTo.containsKey(adminChatId);
    }

    //ID сообщения, которое надо отредактировать
    public void setAdminMessageId(Long adminChatId, Integer messageId) {
        adminMessageId.put(adminChatId, messageId);
    }

    public Integer getAdminMessageId(Long adminChatId) {
        return adminMessageId.get(adminChatId);
    }

    public void clearAdminMessageId(Long adminChatId) {
        adminMessageId.remove(adminChatId);
    }

    //исходный текст сообщения админа
    public void setAdminOriginalText(Long adminChatId, String text) {
        adminOriginalText.put(adminChatId, text);
    }

    public String getAdminOriginalText(Long adminChatId) {
        return adminOriginalText.get(adminChatId);
    }

    public void clearAdminOriginalText(Long adminChatId) {
        adminOriginalText.remove(adminChatId);
    }
}