package com.pdd.pddbottg.service;

import com.pdd.pddbottg.dto.RecommendationQuestionDto;
import com.pdd.pddbottg.entity.ExamSession;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionStorage {
    private final Map<Long, ExamSession> sessions = new ConcurrentHashMap<>();
    private final Map<Long, List<RecommendationQuestionDto>> trainingSessions = new ConcurrentHashMap<>();
    private final Map<Long, Integer> trainingIndexes = new ConcurrentHashMap<>();
    private final Map<Long, Integer> trainingCorrectCount = new ConcurrentHashMap<>(); // ★ добавить

    // Существующие методы для экзамена
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
    }

    // Методы для тренировки (рекомендации)
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
}