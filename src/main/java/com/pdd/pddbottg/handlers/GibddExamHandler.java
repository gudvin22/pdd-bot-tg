package com.pdd.pddbottg.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.dto.ExamResponseDto;
import com.pdd.pddbottg.dto.QuestionDto;
import com.pdd.pddbottg.entity.ExamSession;
import com.pdd.pddbottg.service.MessageSender;
import com.pdd.pddbottg.service.SessionStorage;
import com.pdd.pddbottg.service.TicketProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class GibddExamHandler implements UpdateHandler {
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

            if ("📝 Режим экзамена".equals(text)) {
                try {
                    String json = ticketProcessingService.randomExam(telegramId, firstName);
                    ExamResponseDto ticketResponse = objectMapper.readValue(json, ExamResponseDto.class);

                    // Сохраняем сессию с временем начала
                    ExamSession sessionMap = new ExamSession(
                            ticketResponse.getTicketNumber(),
                            ticketResponse.getQuestions(),
                            true
                    );
                    sessionStorage.putSession(chatId, sessionMap);

                    messageSender.sendMessage(bot, chatId, "📝 Экзамен начался!\n⏱️ У тебя 20 минут.\nБилет № " + ticketResponse.getTicketNumber() + "\nВопрос № 1");

                    QuestionDto question1 = ticketResponse.getQuestions().get(0);
                    String imageUrl = question1.getImageUrlSmall();

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        messageSender.sendFoto(bot, chatId, imageUrl);
                    }

                    messageSender.sendQuestionTextWithInline(bot, chatId, question1.getQuestionText(), question1.getAnswersText());

                } catch (Exception e) {
                    messageSender.sendMessage(bot, chatId, "❌ Не удалось начать экзамен: " + e.getMessage());
                }
                return true;
            }
        }
        return false;
    }
}