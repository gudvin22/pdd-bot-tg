package com.pdd.pddbottg.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdd.pddbottg.dto.SubscriptionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionProcessingService {

    @Value("${myserver.address}")
    private String serverAddress;

    private final HttpClientService httpClientService;


     // Получить информацию о подписке текущего пользователя

    public SubscriptionDto getSubscriptionInfo(String telegramId, String userName) {
        String url = serverAddress + "/api/subscription/info";
        ResponseEntity<String> response = httpClientService.executeWithAuth(
                url, HttpMethod.GET, null, telegramId, userName);

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(response.getBody(), SubscriptionDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения информации о подписке", e);
        }
    }

    // Активировать подписку на 1 месяц

    public SubscriptionDto activateSubscription(String telegramId, String userName) {
        String url = serverAddress + "/api/subscription/activate";
        ResponseEntity<String> response = httpClientService.executeWithAuth(
                url, HttpMethod.POST, null, telegramId, userName);

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(response.getBody(), SubscriptionDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка активации подписки", e);
        }
    }
}