package com.pdd.pddbottg.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.dto.ExamCheckRequestDto;
import com.pdd.pddbottg.dto.WrongAnswerDto;
import com.pdd.pddbottg.entity.ExamSession;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketProcessingService {

    @Value("${myserver.address}")
    private String serverAddress;

    @Value("${bot.message.responseRandomTicketErrorSession}")
    private String responseRandomTicketErrorSession;

    private final RestTemplate restTemplate;
    private final TokenStorageService tokenStorageService;
    private final AuthService authService;
    private final MessageSender messageSender;
    private final SessionStorage sessionStorage;
    private final KeyboardService keyboardService;


    public String randomExam(String telegramId, String userName) {
        String url = serverAddress + "/api/exam/random";
        ResponseEntity<String> response = executeWithAuth(url, HttpMethod.GET, null, telegramId, userName);
        return response.getBody();
    }

    public void checkExam(PddBot bot, String telegramId, String userName, Long chatId) {
        String url = serverAddress + "/api/exam/check";
        ExamSession session = sessionStorage.getSession(chatId);
        if (session == null) {
            messageSender.sendMessageWithKeyboard(bot, chatId, responseRandomTicketErrorSession, keyboardService.mainMenu());

            return;
        }

        ExamCheckRequestDto requestDto = new ExamCheckRequestDto();
        requestDto.setTicketNumber(session.getTicketNumber());
        requestDto.setAnswers(session.getUserAnswers());


        try {
            ResponseEntity<String> response = executeWithAuth(url, HttpMethod.POST, requestDto, telegramId, userName);
            ObjectMapper mapper = new ObjectMapper();
            List<WrongAnswerDto> wrongAnswers = mapper.readValue(
                    response.getBody(),
                    mapper.getTypeFactory().constructCollectionType(List.class, WrongAnswerDto.class)
            );
            if (wrongAnswers.isEmpty()) {
                messageSender.sendMessage(bot, chatId, "Все правильно, поздравляю!");

            }
            messageSender.sendMessage(bot, chatId, "У тебя " + wrongAnswers.size() + " ошибок. Давай разберем их?");
        } catch (Exception e) {
            messageSender.sendMessage(bot, chatId, "Ошибка проверки: " + e.getMessage());
        } finally {
            sessionStorage.removeSession(chatId);
        }

    }

    private ResponseEntity<String> executeWithAuth(String url, HttpMethod method, Object body, String telegramId, String userName) {
        try {
            HttpHeaders headers = tokenStorageService.createAuthHeaders(telegramId);
            HttpEntity<?> entity = (body == null) ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
            return restTemplate.exchange(url, method, entity, String.class);
        } catch (HttpClientErrorException.Forbidden e) {
            // Обновляем токен
            String newToken = authService.registerUser(telegramId, userName);
            tokenStorageService.SaveToken(telegramId, newToken);
            // Повторяем запрос с новым токеном
            HttpHeaders newHeaders = tokenStorageService.createAuthHeaders(telegramId);
            HttpEntity<?> retryEntity = (body == null) ? new HttpEntity<>(newHeaders) : new HttpEntity<>(body, newHeaders);
            return restTemplate.exchange(url, method, retryEntity, String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Ошибка при вызове API: " + e.getMessage(), e);
        }
    }




}
