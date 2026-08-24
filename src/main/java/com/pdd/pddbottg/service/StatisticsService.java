package com.pdd.pddbottg.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pdd.pddbottg.dto.UserStatisticsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final HttpClientService httpClientService;

    @Value("${myserver.address}")
    private String serverAddress;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()); // ← поддержка LocalDateTime

    public UserStatisticsDto getUserStatistics(String telegramId, String userName) {
        String url = serverAddress + "/api/statistics/user-stats";
        log.info("📊 Запрос статистики: {}", url);

        try {
            ResponseEntity<String> response = httpClientService.executeWithAuth(url, HttpMethod.GET, null, telegramId, userName);
            log.info("📊 Статус ответа: {}", response.getStatusCode());
            log.info("📊 Тело ответа: {}", response.getBody());

            return objectMapper.readValue(response.getBody(), UserStatisticsDto.class);
        } catch (Exception e) {
            log.error("❌ Ошибка получения статистики: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка получения статистики: " + e.getMessage(), e);
        }
    }

    public String getRecommendation(String telegramId, String userName) {
        String url = serverAddress + "/api/ai/analyze-statistics";
        log.info("🧠 Запрос AI-анализа: {}", url);

        try {
            ResponseEntity<String> response = httpClientService.executeWithAuth(url, HttpMethod.GET, null, telegramId, userName);
            log.info("🧠 Статус ответа: {}", response.getStatusCode());
            return response.getBody();
        } catch (Exception e) {
            log.error("❌ Ошибка получения AI-анализа: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка получения AI-анализа: " + e.getMessage(), e);
        }
    }
}