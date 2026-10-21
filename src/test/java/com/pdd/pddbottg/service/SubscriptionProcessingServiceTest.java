package com.pdd.pddbottg.service;

import com.pdd.pddbottg.*;
import com.pdd.pddbottg.dto.SubscriptionDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionProcessingServiceTest {

    @Mock
    private HttpClientService httpClientService;

    @Mock
    private MessageSender messageSender;

    @Mock
    private KeyboardService keyboardService;

    @Mock
    private PddBot bot;

    @InjectMocks
    private SubscriptionProcessingService subscriptionService;

    // ========== Тесты парсинга JSON ==========

    @Test
    void getSubscriptionInfo_shouldParseJsonWithLocalDateTime() {
        String jsonResponse = "{\"subscription\":\"PREMIUM\",\"subscriptionEndDate\":\"2026-11-04T19:55:47.387844\",\"active\":true,\"message\":\"Подписка активна\"}";
        ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);

        when(httpClientService.executeWithAuth(anyString(), eq(HttpMethod.GET), any(), anyString(), anyString()))
                .thenReturn(responseEntity);

        SubscriptionDto result = subscriptionService.getSubscriptionInfo("12345", "Roman");

        assertNotNull(result);
        assertEquals("PREMIUM", result.getSubscription());
        assertTrue(result.isActive());
        assertNotNull(result.getSubscriptionEndDate());
        assertEquals(2026, result.getSubscriptionEndDate().getYear());
    }

    // ========== Тесты метода check() ==========

    @Test
    void check_whenSubscriptionActive_returnsTrue() {
        String jsonResponse = "{\"subscription\":\"PREMIUM\",\"subscriptionEndDate\":\"2026-11-04T19:55:47.387844\",\"active\":true,\"message\":\"Подписка активна\"}";
        when(httpClientService.executeWithAuth(anyString(), any(), any(), anyString(), anyString()))
                .thenReturn(ResponseEntity.ok(jsonResponse));

        boolean result = subscriptionService.check(bot, 123L, "12345", "Roman");

        assertTrue(result, "Должно вернуть true, если подписка активна");
        verify(messageSender, never()).sendMessage(any(), anyLong(), anyString());
        verify(messageSender, never()).sendMessageInlineKeyboard(any(), anyLong(), anyString(), any());
    }

    @Test
    void check_whenSubscriptionInactive_returnsFalseAndSendsMessages() {
        String jsonResponse = "{\"subscription\":\"FREE\",\"subscriptionEndDate\":null,\"active\":false,\"message\":\"Подписки нет\"}";
        when(httpClientService.executeWithAuth(anyString(), any(), any(), anyString(), anyString()))
                .thenReturn(ResponseEntity.ok(jsonResponse));

        InlineKeyboardMarkup mockKeyboard = new InlineKeyboardMarkup();
        when(keyboardService.activateSubscriptionKeyboard()).thenReturn(mockKeyboard);

        boolean result = subscriptionService.check(bot, 123L, "12345", "Roman");

        assertFalse(result, "Должно вернуть false, если подписки нет");
        verify(messageSender, times(1)).sendMessage(eq(bot), eq(123L), contains("🔒"));
        verify(messageSender, times(1)).sendMessageInlineKeyboard(eq(bot), eq(123L), anyString(), eq(mockKeyboard));
    }

    @Test
    void check_whenExceptionThrown_returnsFalseAndSendsErrorMessage() {
        when(httpClientService.executeWithAuth(anyString(), any(), any(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Сервер недоступен"));

        boolean result = subscriptionService.check(bot, 123L, "12345", "Roman");

        assertFalse(result);
        verify(messageSender, times(1)).sendMessage(eq(bot), eq(123L), contains("❌ Не удалось проверить подписку"));
    }
}