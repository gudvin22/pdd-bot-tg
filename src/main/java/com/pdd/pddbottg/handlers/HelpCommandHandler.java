package com.pdd.pddbottg.handlers;

import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.service.MessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
@Component
@RequiredArgsConstructor
public class HelpCommandHandler implements UpdateHandler {
    private final MessageSender messageSender;

    @Value("${bot.message.help}")
    private String helpMessage;

    @Override
    public boolean handle(PddBot bot, Update update)  {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if ("/help".equals(text)) {
                messageSender.sendMessage(bot, chatId,helpMessage);
                return true;
            }
        }
        return false;

    }
}
