package com.pdd.pddbottg.service;

import com.pdd.pddbottg.PddBot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TicketProcessingService {

    @Value("${myserver.address}")
    private String serverAddress;

    @Value("${bot.message.responseRandomTicket}")
    private String responseRandomTicket;


    private final RestTemplate restTemplate;
    private final TokenStorageService tokenStorageService;
    private final AuthService authService;
    private final MessageSender messageSender;

    public String randomExam(String telegramId, String userName) {
        String url = serverAddress + "/api/exam/random";
        try {
            HttpHeaders headers = tokenStorageService.createAuthHeaders(telegramId);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException.Forbidden e) {
            // 1. Получить новый токен
            String newToken = authService.registerUser(telegramId, userName);
            // 2. Сохранить новый токен в БД
            tokenStorageService.SaveToken(telegramId, newToken);
            // 3. Повторить запрос с новым токеном
            HttpHeaders newHeaders = tokenStorageService.createAuthHeaders(telegramId);
            HttpEntity<?> newEntity = new HttpEntity<>(newHeaders);
            ResponseEntity<String> retryResponse = restTemplate.exchange(url, HttpMethod.GET, newEntity, String.class);
            return retryResponse.getBody();
        } catch (RestClientException e) {
            throw new RuntimeException("Ошибка при вызове API: " + e.getMessage(), e);
        }
    }

    public void checkExam(PddBot bot, String telegramId, String userName, Long chatId) {
        String url = serverAddress + "/api/exam/check";
        //дописать логику
//        try {
//            HttpHeaders headers = tokenStorageService.createAuthHeaders(telegramId);
//            HttpEntity<?> entity = new HttpEntity<>(headers);
//            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
//            String answer = response.getBody();
//        } catch (HttpClientErrorException.Forbidden e) {
//            // 1. Получить новый токен
//            String newToken = authService.registerUser(telegramId, userName);
//            // 2. Сохранить новый токен в БД
//            tokenStorageService.SaveToken(telegramId, newToken);
//            // 3. Повторить запрос с новым токеном
//            HttpHeaders newHeaders = tokenStorageService.createAuthHeaders(telegramId);
//            HttpEntity<?> newEntity = new HttpEntity<>(newHeaders);
//            ResponseEntity<String> retryResponse = restTemplate.exchange(url, HttpMethod.GET, newEntity, String.class);
//            String answer = retryResponse.getBody();
//        } catch (RestClientException e) {
//            throw new RuntimeException("Ошибка при вызове API: " + e.getMessage(), e);
//        }
        // пока ничего не делаем отправляем в чат рандомное сообщение
        messageSender.sendMessage(bot,chatId,responseRandomTicket);

    }


}
