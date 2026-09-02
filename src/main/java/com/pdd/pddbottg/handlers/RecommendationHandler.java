package com.pdd.pddbottg.handlers;

import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.dto.RecommendationQuestionDto;
import com.pdd.pddbottg.service.MessageSender;
import com.pdd.pddbottg.service.SessionStorage;
import com.pdd.pddbottg.service.TicketProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RecommendationHandler implements UpdateHandler {
    private final TicketProcessingService ticketProcessingService;
    private final MessageSender messageSender;
    private final SessionStorage sessionStorage;

    @Override
    public boolean handle(PddBot bot, Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String telegramId = String.valueOf(update.getMessage().getFrom().getId());
            String userName = update.getMessage().getFrom().getFirstName();

            if ("🎯 Рекомендация".equals(text)) {
                try {
                    List<RecommendationQuestionDto> questions = ticketProcessingService.getRecommendedQuestions(telegramId, userName);

                    if (questions == null || questions.isEmpty()) {
                        messageSender.sendMessage(bot, chatId, "😕 У тебя пока нет слабых тем. Реши несколько билетов, и я подберу рекомендации!");
                        return true;
                    }

                    // Сохраняем вопросы в сессию
                    sessionStorage.putTrainingQuestions(chatId, questions);

                    // Показываем первый вопрос
                    showQuestion(bot, chatId, 0);

                } catch (Exception e) {
                    messageSender.sendMessage(bot, chatId, "❌ Ошибка получения рекомендаций: " + e.getMessage());
                }
                return true;
            }
        }
        return false;
    }

    private void showQuestion(PddBot bot, Long chatId, int index) {
        List<RecommendationQuestionDto> questions = sessionStorage.getTrainingQuestions(chatId);
        if (questions == null || index >= questions.size()) {
            messageSender.sendMessage(bot, chatId, "⚠️ Что-то пошло не так. Попробуй ещё раз.");
            return;
        }

        RecommendationQuestionDto question = questions.get(index);

        // Отправляем картинку
        if (question.getImageUrlSmall() != null && !question.getImageUrlSmall().isEmpty()) {
            try {
                messageSender.sendFoto(bot, chatId, question.getImageUrlSmall());
            } catch (Exception e) {
                // игнорируем
            }
        }

        // Отправляем вопрос с вариантами
        messageSender.sendQuestionTextWithInline(
                bot,
                chatId,
                "📝 Вопрос " + (index + 1) + " из " + questions.size() + "\n\n" + question.getQuestionText(),
                question.getAnswers()
        );
    }
}