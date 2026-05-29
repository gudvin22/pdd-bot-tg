package com.pdd.pddbottg.handlers;

import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.service.AuthService;
import com.pdd.pddbottg.service.KeyboardService;
import com.pdd.pddbottg.service.MessageSender;
import com.pdd.pddbottg.service.TokenStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class StartCommandHandler implements UpdateHandler {

    private final MessageSender messageSender;
    private final AuthService authService;
    private final TokenStorageService tokenStorageService;
    private final KeyboardService keyboardService;

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

                tokenStorageService.SaveToken(telegramId, token);
                //String message = tokenStorageService.GetJwtToken(telegramId).orElseThrow(() -> new RuntimeException("Токен не найден"));;
                //messageSender.sendMessage(bot, chatId, startMessage);
                messageSender.sendMessageWithKeyboard(bot,chatId,startMessage,keyboardService.mainMenu());


                return true;
            }
        }
        return false;
    }
}