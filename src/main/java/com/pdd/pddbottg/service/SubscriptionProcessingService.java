package com.pdd.pddbottg.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pdd.pddbottg.PddBot;
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
    private final MessageSender messageSender;
    private final KeyboardService keyboardService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public SubscriptionDto getSubscriptionInfo(String telegramId, String userName) {
        String url = serverAddress + "/api/subscription/info";
        ResponseEntity<String> response = httpClientService.executeWithAuth(
                url, HttpMethod.GET, null, telegramId, userName);

        try {
            return objectMapper.readValue(response.getBody(), SubscriptionDto.class);
        } catch (Exception e) {
            log.error("❌ Ошибка парсинга SubscriptionDto: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка получения информации о подписке", e);
        }
    }

    public SubscriptionDto activateSubscription(String telegramId, String userName) {
        String url = serverAddress + "/api/subscription/activate";
        ResponseEntity<String> response = httpClientService.executeWithAuth(
                url, HttpMethod.POST, null, telegramId, userName);

        try {
            return objectMapper.readValue(response.getBody(), SubscriptionDto.class);
        } catch (Exception e) {
            log.error("❌ Ошибка парсинга SubscriptionDto при активации: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка активации подписки", e);
        }
    }

    /**
     * Проверяет активную подписку.
     * Если активна — true.
     * Если нет — отправляет сообщение + кнопку «Активировать» и возвращает false.
     */
    public boolean check(PddBot bot, Long chatId, String telegramId, String userName) {
        try {
            SubscriptionDto dto = getSubscriptionInfo(telegramId, userName);

            if (dto.isActive()) {
                return true;
            }

            messageSender.sendMessage(bot, chatId,
                    "🔒 Эта функция доступна только с подпиской.\n\n" + dto.getMessage());

            messageSender.sendMessageInlineKeyboard(bot, chatId,
                    "Хотите оформить подписку?",
                    keyboardService.activateSubscriptionKeyboard());

            return false;

        } catch (Exception e) {
            log.error("Ошибка проверки подписки: {}", e.getMessage(), e);
            messageSender.sendMessage(bot, chatId, "❌ Не удалось проверить подписку.");
            return false;
        }
    }
}