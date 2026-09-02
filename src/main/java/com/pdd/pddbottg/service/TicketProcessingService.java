package com.pdd.pddbottg.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.dto.*;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

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
    private final HttpClientService httpClientService;


    public String randomExam(String telegramId, String userName) {
        String url = serverAddress + "/api/exam/random";
        ResponseEntity<String> response = httpClientService.executeWithAuth(url, HttpMethod.GET, null, telegramId, userName);
        return response.getBody();
    }

    public ExamResponseDto getTicket(int ticketNumber, String telegramId, String userName) {
        String url = serverAddress + "/api/exam/ticket/" + ticketNumber;
        ResponseEntity<String> response = httpClientService.executeWithAuth(url, HttpMethod.GET, null, telegramId, userName);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(response.getBody(), ExamResponseDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения билета " + ticketNumber, e);
        }
    }

    public void checkExam(PddBot bot, String telegramId, String userName, Long chatId) {
        String url = serverAddress + "/api/exam/check";
        ExamSession session = sessionStorage.getSession(chatId);
        if (session == null) {
            messageSender.sendMessageWithReplyKeyboard(bot, chatId, responseRandomTicketErrorSession, keyboardService.mainMenu());

            return;
        }

        ExamCheckRequestDto requestDto = new ExamCheckRequestDto();
        requestDto.setTicketNumber(session.getTicketNumber());
        requestDto.setAnswers(session.getUserAnswers());


        try {
            ResponseEntity<String> response = httpClientService.executeWithAuth(url, HttpMethod.POST, requestDto, telegramId, userName);
            ObjectMapper mapper = new ObjectMapper();
            List<WrongAnswerDto> wrongAnswers = mapper.readValue(
                    response.getBody(),
                    mapper.getTypeFactory().constructCollectionType(List.class, WrongAnswerDto.class)
            );

            session.setWrongAnswers(wrongAnswers); //создаем сессию неправильных ответов
            session.setCurrentErrorIndex(0);

            if (wrongAnswers.isEmpty()) {
                messageSender.sendMessage(bot, chatId, "Все правильно, поздравляю!");
                sessionStorage.removeSession(chatId);

            }
            else {

                messageSender.sendMessage(bot, chatId, "У тебя " + wrongAnswers.size() + " ошибок. ");

                InlineKeyboardMarkup keyboardMarkup = keyboardService.viewErrorsKeyboard();
                messageSender.sendMessageInlineKeyboard(bot, chatId, "Давай разберем их ?", keyboardMarkup);

            }
            messageSender.sendMessageWithReplyKeyboard(bot, chatId, "Или перейдем в главное меню", keyboardService.mainMenu());


            } catch (Exception e) {
            messageSender.sendMessage(bot, chatId, "Ошибка проверки: " + e.getMessage());
        }
    }

    public String getAiAnalysis(String telegramId, String userName, AiAnalysisTicketRequestDto request) {
        String url = serverAddress + "/api/ai/analyze-errors";
        ResponseEntity<String> response = httpClientService.executeWithAuth(url, HttpMethod.POST, request, telegramId, userName);
        return response.getBody();
    }

    public List<TicketStatusDto> getTicketsStatus(String telegramId, String userName) {
        String url = serverAddress + "/api/statistics/tickets-status";
        ResponseEntity<String> response = httpClientService.executeWithAuth(url, HttpMethod.GET, null, telegramId, userName);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(response.getBody(), new  TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Ошибка парсинга статусов билетов", e);
        }
    }

    public List<RecommendationQuestionDto> getRecommendedQuestions(String telegramId, String userName) {
        String url = serverAddress + "/api/exam/recommended-questions";
        ResponseEntity<String> response = httpClientService.executeWithAuth(url, HttpMethod.GET, null, telegramId, userName);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(response.getBody(), new  TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения рекомендованных вопросов", e);
        }
    }




}
