package com.pdd.pddbottg.handlers;

import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.dto.SubscriptionDto;
import com.pdd.pddbottg.service.KeyboardService;
import com.pdd.pddbottg.service.MessageSender;
import com.pdd.pddbottg.service.SubscriptionProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionHandler implements UpdateHandler {

    private final SubscriptionProcessingService subscriptionProcessingService;
    private final MessageSender messageSender;
    private final KeyboardService keyboardService;

    @Override
    public boolean handle(PddBot bot, Update update) {

        //  Обработка нажатия кнопки «⭐ Подписка»
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if ("⭐ Подписка".equals(text)) {
                String telegramId = String.valueOf(update.getMessage().getFrom().getId());
                String userName = update.getMessage().getFrom().getFirstName();

                try {
                    SubscriptionDto dto = subscriptionProcessingService.getSubscriptionInfo(telegramId, userName);
                    messageSender.sendMessage(bot, chatId, dto.getMessage());

                    if (!dto.isActive()) {
                        InlineKeyboardMarkup keyboard = keyboardService.activateSubscriptionKeyboard();
                        messageSender.sendMessageInlineKeyboard(bot, chatId,
                                "Хотите оформить подписку?", keyboard);
                    }
                } catch (Exception e) {
                    log.error("Ошибка получения подписки: {}", e.getMessage(), e);
                    messageSender.sendMessage(bot, chatId, "❌ Не удалось получить информацию о подписке.");
                }
                return true;
            }
        }

        //  Обработка callback «Активировать подписку»
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            if ("activate_subscription".equals(callbackData)) {
                String telegramId = String.valueOf(update.getCallbackQuery().getFrom().getId());
                String userName = update.getCallbackQuery().getFrom().getFirstName();

                try {
                    SubscriptionDto dto = subscriptionProcessingService.activateSubscription(telegramId, userName);
                    messageSender.sendMessage(bot, chatId, "🎉 " + dto.getMessage());
                } catch (Exception e) {
                    log.error("Ошибка активации подписки: {}", e.getMessage(), e);
                    messageSender.sendMessage(bot, chatId, "❌ Не удалось активировать подписку.");
                }
                return true;
            }
        }

        return false;
    }
}