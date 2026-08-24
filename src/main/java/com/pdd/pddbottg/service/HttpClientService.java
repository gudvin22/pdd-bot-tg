package com.pdd.pddbottg.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class HttpClientService {
    private final RestTemplate restTemplate;
    private final TokenStorageService tokenStorageService;
    private final AuthService authService;

    public ResponseEntity<String> executeWithAuth(String url, HttpMethod method, Object body, String telegramId, String userName) {
        try {
            HttpHeaders headers = tokenStorageService.createAuthHeaders(telegramId);
            HttpEntity<?> entity = (body == null) ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
            return restTemplate.exchange(url, method, entity, String.class);
        } catch (HttpClientErrorException.Forbidden e) {
            String newToken = authService.registerUser(telegramId, userName);
            tokenStorageService.SaveToken(telegramId, newToken);
            HttpHeaders newHeaders = tokenStorageService.createAuthHeaders(telegramId);
            HttpEntity<?> retryEntity = (body == null) ? new HttpEntity<>(newHeaders) : new HttpEntity<>(body, newHeaders);
            return restTemplate.exchange(url, method, retryEntity, String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Ошибка при вызове API: " + e.getMessage(), e);
        }
    }
}