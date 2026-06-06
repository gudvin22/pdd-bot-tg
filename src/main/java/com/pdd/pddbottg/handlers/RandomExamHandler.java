package com.pdd.pddbottg.handlers;

import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.dto.ExamResponseDto;
import com.pdd.pddbottg.dto.QuestionDto;
import com.pdd.pddbottg.entity.ExamSession;
import com.pdd.pddbottg.service.MessageSender;
import com.pdd.pddbottg.service.SessionStorage;
import com.pdd.pddbottg.service.TicketProcessingService;
import com.pdd.pddbottg.service.TokenStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Update;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RandomExamHandler implements UpdateHandler {
    private final MessageSender messageSender;
    private final TicketProcessingService ticketProcessingService;
    private final SessionStorage sessionStorage;

    ObjectMapper objectMapper = new ObjectMapper();



    @Override
    public boolean handle(PddBot bot, Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String telegramId = String.valueOf(update.getMessage().getFrom().getId());
            String firstName = update.getMessage().getFrom().getFirstName();
            if ("🎲 Случайный билет".equals(text)) {
                try {
                    String json = ticketProcessingService.randomExam(telegramId, firstName);
                    ExamResponseDto ticketResponse = objectMapper.readValue(json, ExamResponseDto.class);
                    messageSender.sendMessage(bot, chatId, "Билет номер: " + ticketResponse.getTicketNumber() +
                            "\nВопрос номер: 1");
                    ExamSession sessionMap = new ExamSession(ticketResponse.getTicketNumber(), ticketResponse.getQuestions());
                    sessionStorage.putSession(chatId, sessionMap);

                    //потестим клаву
                    QuestionDto question1 = ticketResponse.getQuestions().get(0);
                    String imageUrl = question1.getImageUrlSmall();


                    if(imageUrl != null && !imageUrl.isEmpty()) {
                        messageSender.sendFoto(bot, chatId, imageUrl);
                    }

                    messageSender.sendQuestionInline(bot, chatId, question1.getQuestionText(), question1.getAnswersText());


                } catch (Exception e) {
                    messageSender.sendMessage(bot, chatId, "Не удалось загрузить билет. Ошибка: " + e.getMessage());
                }

                return true;
            }
        }
        return false;

    }
}
