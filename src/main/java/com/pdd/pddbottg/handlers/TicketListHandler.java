package com.pdd.pddbottg.handlers;

import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.dto.TicketStatusDto;
import com.pdd.pddbottg.service.MessageSender;
import com.pdd.pddbottg.service.TicketProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TicketListHandler implements UpdateHandler {
    private final TicketProcessingService  ticketProcessingService;
    private final MessageSender messageSender;

    @Override
    public boolean handle(PddBot bot, Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String telegramId = String.valueOf(update.getMessage().getFrom().getId());
            String firstName = update.getMessage().getFrom().getFirstName();

            if ("📚 Все билеты".equals(text)) {
                List<TicketStatusDto> statuses = ticketProcessingService.getTicketsStatus(telegramId, firstName);
                StringBuilder sb = new StringBuilder("📚 Все билеты\n\n");

                //создаем клавиатуру с билетами
                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                List<InlineKeyboardButton> currentRow = new ArrayList<>();

                int counter = 0;
                for (TicketStatusDto dto : statuses) {

                    String emoji = switch (dto.getStatus()) {
                        case CORRECT -> "✅";
                        case INCORRECT -> "❌";
                        default -> "⚪";
                    };

                    // Создаём кнопку
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(emoji + " " + dto.getTicketNumber());
                    button.setCallbackData("ticket_" + dto.getTicketNumber());
                    currentRow.add(button);

                    counter++;
                    if (counter % 8 == 0) { // каждые 5 кнопок – новый ряд
                        rows.add(currentRow);
                        currentRow = new ArrayList<>();
                    }
                }
                // Добавляем последний ряд, если он непустой
                if (!currentRow.isEmpty()) {
                    rows.add(currentRow);
                }
                keyboard.setKeyboard(rows);
                sb.append("✅ — решён без ошибок\n");
                sb.append("❌ — есть ошибки\n");
                sb.append("⚪ — не решён\n");
                                // Отправляем
                messageSender.sendMessageInlineKeyboard(bot, chatId, sb.toString(), keyboard);


                return true;
            }
        }
        return false;
    }






}
