package com.pdd.pddbottg.service;

import com.pdd.pddbottg.AuthService;
import com.pdd.pddbottg.dto.TelegramRegistrationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthService authService;

    private final String testServerAddress = "http://localhost:8080";
    private final String telegramId = "12345";
    private final String userName = "Roman";

    @BeforeEach
    void setUp() {
        // Так как @Value не работает в обычных тестах, подставляем адрес вручную
        ReflectionTestUtils.setField(authService, "serverAddress", testServerAddress);
    }

    @Test
    void registerUser_whenSuccess_shouldReturnJwtToken() {
        // 1. Подготовка
        String expectedToken = "jwt-token-abc-123";
        String expectedUrl = testServerAddress + "/api/auth/telegram-register";

        // Говорим моку, что при вызове postForObject вернется токен
        when(restTemplate.postForObject(
                eq(expectedUrl),
                any(TelegramRegistrationDto.class),
                eq(String.class)))
                .thenReturn(expectedToken);

        // 2. Действие
        String actualToken = authService.registerUser(telegramId, userName);

        // 3. Проверка
        assertEquals(expectedToken, actualToken);

        // Проверяем, что RestTemplate вызвался ровно 1 раз с правильным URL
        verify(restTemplate, times(1)).postForObject(
                eq(expectedUrl),
                any(TelegramRegistrationDto.class),
                eq(String.class));
    }

    @Test
    void registerUser_whenServerThrowsException_shouldThrowRuntimeException() {
        // 1. Подготовка
        // Говорим моку, что при вызове будет ошибка (например, сервер недоступен)
        when(restTemplate.postForObject(
                anyString(),
                any(TelegramRegistrationDto.class),
                eq(String.class)))
                .thenThrow(new RestClientException("Сервер упал"));

        // 2. Действие + Проверка
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.registerUser(telegramId, userName);
        });

        // Проверяем, что ошибка обернута в RuntimeException
        assertNotNull(exception);
        // Внутри RuntimeException должен лежать наш RestClientException
        assertInstanceOf(RestClientException.class, exception.getCause());
    }
}
