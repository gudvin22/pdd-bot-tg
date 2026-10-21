package com.pdd.pddbottg.service;

import com.pdd.pddbottg.AuthService;
import com.pdd.pddbottg.HttpClientService;
import com.pdd.pddbottg.TokenStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpClientServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TokenStorageService tokenStorageService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private HttpClientService httpClientService;

    private final String url = "http://localhost:8080/api/test";
    private final String telegramId = "12345";
    private final String userName = "Roman";

    @Test
    void executeWithAuth_whenSuccess_shouldReturnResponse() {
        HttpHeaders mockHeaders = new HttpHeaders();
        when(tokenStorageService.createAuthHeaders(telegramId)).thenReturn(mockHeaders);

        ResponseEntity<String> expectedResponse = ResponseEntity.ok("Success Data");
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<String> actualResponse = httpClientService.executeWithAuth(url, HttpMethod.GET, null, telegramId, userName);

        assertNotNull(actualResponse);
        assertEquals("Success Data", actualResponse.getBody());
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
        verify(authService, never()).registerUser(anyString(), anyString());
    }

    @Test
    void executeWithAuth_when403Forbidden_shouldRetryWithNewToken() {
        HttpHeaders mockHeaders = new HttpHeaders();
        when(tokenStorageService.createAuthHeaders(telegramId)).thenReturn(mockHeaders);

        // ИСПРАВЛЕНО: используем статический метод create, который сам вернёт Forbidden
        HttpClientErrorException forbiddenException = HttpClientErrorException.create(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                new HttpHeaders(),
                null,
                null
        );

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenThrow(forbiddenException)
                .thenReturn(ResponseEntity.ok("Success after retry"));

        when(authService.registerUser(telegramId, userName)).thenReturn("new-jwt-token");

        ResponseEntity<String> actualResponse = httpClientService.executeWithAuth(url, HttpMethod.GET, null, telegramId, userName);

        assertEquals("Success after retry", actualResponse.getBody());

        verify(authService, times(1)).registerUser(telegramId, userName);
        verify(tokenStorageService, times(1)).SaveToken(telegramId, "new-jwt-token");
        verify(restTemplate, times(2)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void executeWithAuth_whenOtherException_shouldThrowRuntimeException() {
        HttpHeaders mockHeaders = new HttpHeaders();
        when(tokenStorageService.createAuthHeaders(telegramId)).thenReturn(mockHeaders);

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)))
                .thenThrow(new RestClientException("Сервер упал"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            httpClientService.executeWithAuth(url, HttpMethod.GET, null, telegramId, userName);
        });

        assertTrue(exception.getMessage().contains("Ошибка при вызове API: Сервер упал"));
    }
}