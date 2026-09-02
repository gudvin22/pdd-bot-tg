package com.pdd.pddbottg.handlers;

import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.dto.UserStatisticsDto;
import com.pdd.pddbottg.service.KeyboardService;
import com.pdd.pddbottg.service.MessageSender;
import com.pdd.pddbottg.service.SessionStorage;
import com.pdd.pddbottg.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class RefreshCommandHandler implements UpdateHandler {

    private final MessageSender messageSender;
    private final KeyboardService keyboardService;
    private final SessionStorage sessionStorage;
    private final StatisticsService statisticsService; // ← добавить

    @Override
    public boolean handle(PddBot bot, Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if ("/refresh".equals(text)) {
                String telegramId = String.valueOf(update.getMessage().getFrom().getId());
                String firstName = update.getMessage().getFrom().getFirstName();

                // Сбрасываем сессию
                sessionStorage.removeSession(chatId);

                // Проверяем, решал ли пользователь билеты
                try {
                    UserStatisticsDto stats = statisticsService.getUserStatistics(telegramId, firstName);
                    if (stats.getTotalAttempts() > 0) {
                        // Уже решал → показываем главное меню
                        messageSender.sendMessageWithReplyKeyboard(
                                bot,
                                chatId,
                                "🔄 Бот перезапущен! Продолжим подготовку.",
                                keyboardService.mainMenu()
                        );
                    } else {
                        // Новый пользователь → только случайный билет
                        messageSender.sendMessageWithReplyKeyboard(
                                bot,
                                chatId,
                                "🔄 Бот перезапущен! Давай начнём.",
                                keyboardService.mainRandomMenu()
                        );
                    }
                } catch (Exception e) {
                    // Если ошибка — безопасный вариант: одноразовое меню
                    messageSender.sendMessageWithReplyKeyboard(
                            bot,
                            chatId,
                            "🔄 Бот перезапущен!",
                            keyboardService.mainRandomMenu()
                    );
                }

                return true;
            }
        }
        return false;
    }
}