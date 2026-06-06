package com.pdd.pddbottg.service;

import com.pdd.pddbottg.entity.ExamSession;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionStorage {
    private final Map<Long, ExamSession> sessions = new ConcurrentHashMap<>();

    public void putSession(Long chatId, ExamSession examSession) {
        sessions.put(chatId, examSession);
    }

    public ExamSession getSession(Long chatId) {
        return sessions.get(chatId);
    }

    public void removeSession(Long chatId) {
        sessions.remove(chatId);
    }
}
