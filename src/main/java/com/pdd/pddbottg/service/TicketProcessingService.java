package com.pdd.pddbottg.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TicketProcessingService {
    @Value("${myserver.address}")
    private String serverAddress;
    private final RestTemplate restTemplate;
    private final TokenStorageService tokenStorageService;

    public String randomExam(String telegramId){
        String url = serverAddress + "/api/exam/random";
        HttpHeaders headers = tokenStorageService.createAuthHeaders(telegramId);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (RestClientException e) {
            throw new RuntimeException("Ошибка при вызове API: " + e.getMessage(), e);
        }
    }

}
