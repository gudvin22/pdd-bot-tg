package com.pdd.pddbottg.handlers;

import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.service.AuthService;
import com.pdd.pddbottg.service.MessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class StartCommandHandler implements UpdateHandler {

    private final MessageSender messageSender;
    private final AuthService authService;

    @Value("${bot.message.start}")
    private String startMessage;

    @Override
    public boolean handle(PddBot bot, Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            if ("/start".equals(text)) {
                String firstName = update.getMessage().getFrom().getFirstName();
                String telegramId = String.valueOf(update.getMessage().getFrom().getId());
                String  token = authService.registerUser(telegramId, firstName);

                messageSender.sendMessage(bot, chatId, startMessage + " " + token);

                return true;
            }
        }
        return false;
    }
}