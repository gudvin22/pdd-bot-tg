package com.pdd.pddbottg.handlers;

import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.service.MessageSender;
import com.pdd.pddbottg.service.TicketProcessingService;
import com.pdd.pddbottg.service.TokenStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class RandomExamHandler implements UpdateHandler {
    private final MessageSender messageSender;
    private final TicketProcessingService ticketProcessingService;


    @Override
    public boolean handle(PddBot bot, Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String telegramId = String.valueOf(update.getMessage().getFrom().getId());
            if ("🎲 Случайный билет".equals(text)) {
                String json = ticketProcessingService.randomExam(telegramId);
                String shortJson = json.substring(0, Math.min(1000, json.length()));
                messageSender.sendMessage(bot,chatId,shortJson);
                return true;
            }
        }
        return false;

    }
}
