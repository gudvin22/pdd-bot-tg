package com.pdd.pddbottg.handlers;

import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.dto.ExamResponseDto;
import com.pdd.pddbottg.dto.QuestionDto;
import com.pdd.pddbottg.entity.ExamSession;
import com.pdd.pddbottg.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class CallbackHandler implements UpdateHandler {
    private final SessionStorage sessionStorage;
    private final MessageSender messageSender;
    private final TicketProcessingService ticketProcessingService;
    private final KeyboardService keyboardService;
    private final TokenStorageService tokenStorageService;
    private final StatisticsService statisticsService;
    private final TrainingService trainingService; // ★ добавить

    @Value("${bot.message.responseRandomQuestion}")
    private String responseMessage;

    @Value("${bot.message.responseRandomTicketErrorSession}")
    private String responseRandomTicketErrorSession;

    @Override
    public boolean handle(PddBot bot, Update update) {

        if (update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callbackData = update.getCallbackQuery().getData();

            // РЕЖИМ ТРЕНИРОВКИ
            if (sessionStorage.isTrainingMode(chatId)) {
                if (callbackData.startsWith("answer_")) {
                    int userAnswerIndex = Integer.parseInt(callbackData.substring(7));
                    trainingService.handleAnswer(bot, chatId, userAnswerIndex);
                    return true;
                }
                if (callbackData.equals("training_next")) {
                    trainingService.showNextQuestion(bot, chatId);
                    return true;
                }
                if (callbackData.equals("training_finish")) {
                    trainingService.showResult(bot, chatId);
                    return true;
                }
            }

            // Обработка выбора билета из списка
            if (callbackData.startsWith("ticket_")) {
                int ticketNumber = Integer.parseInt(callbackData.substring(7));
                String telegramId = String.valueOf(update.getCallbackQuery().getFrom().getId());
                String userName = update.getCallbackQuery().getFrom().getFirstName();

                try {
                    ExamResponseDto ticketResponse = ticketProcessingService.getTicket(ticketNumber, telegramId, userName);
                    ExamSession ticketSession = new ExamSession(ticketResponse.getTicketNumber(), ticketResponse.getQuestions());
                    sessionStorage.putSession(chatId, ticketSession);

                    messageSender.sendMessage(bot, chatId, "Билет № " + ticketResponse.getTicketNumber() + "\nВопрос № 1");
                    QuestionDto question = ticketResponse.getQuestions().get(0);
                    String imageUrl = question.getImageUrlSmall();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        messageSender.sendFoto(bot, chatId, imageUrl);
                    }
                    messageSender.sendQuestionTextWithInline(bot, chatId, question.getQuestionText(), question.getAnswersText());

                } catch (Exception e) {
                    messageSender.sendMessage(bot, chatId, "❌ Не удалось загрузить билет: " + e.getMessage());
                }
                return true;
            }

            // AI-анализ статистики
            if (callbackData.equals("ai_statistics_analysis")) {
                String telegramId = String.valueOf(update.getCallbackQuery().getFrom().getId());
                String userName = update.getCallbackQuery().getFrom().getFirstName();

                messageSender.sendMessage(bot, chatId, "🧠 Генерирую AI-анализ...");

                try {
                    String analysis = statisticsService.getRecommendation(telegramId, userName);
                    messageSender.sendMessage(bot, chatId, analysis);
                } catch (Exception e) {
                    messageSender.sendMessage(bot, chatId, "❌ Ошибка: " + e.getMessage());
                }
                return true;
            }

            // Обычный экзамен
            ExamSession session = sessionStorage.getSession(chatId);
            if (session == null) {
                messageSender.sendMessageWithReplyKeyboard(bot, chatId, responseRandomTicketErrorSession, keyboardService.mainMenu());
                return true;
            }

            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(update.getCallbackQuery().getId());
            answer.setText(responseMessage);
            try {
                bot.execute(answer);
            } catch (Exception e) {
                messageSender.sendMessage(bot, chatId, "Не удалось отправить ответ. Ошибка: " + e.getMessage());
            }

            // Проверяем, что это ответ на вопрос (начинается с "answer_")
            if (!callbackData.startsWith("answer_")) {
                return false;
            }

            int userAnswerIndex = Integer.parseInt(callbackData.substring(7));
            session.getUserAnswers().set(session.getCurrentQuestionIndex(), userAnswerIndex);
            session.setCurrentQuestionIndex(session.getCurrentQuestionIndex() + 1);

            if (session.getCurrentQuestionIndex() < session.getQuestions().size()) {
                QuestionDto question = session.getQuestions().get(session.getCurrentQuestionIndex());
                String imageUrl = question.getImageUrlSmall();

                messageSender.sendMessage(bot, chatId, "Вопрос № " + (session.getCurrentQuestionIndex() + 1));

                try {
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        messageSender.sendFoto(bot, chatId, imageUrl);
                    }
                    messageSender.sendQuestionTextWithInline(bot, chatId, question.getQuestionText(), question.getAnswersText());
                } catch (Exception e) {
                    messageSender.sendMessage(bot, chatId, "Не удалось загрузить билет. Ошибка: " + e.getMessage());
                }
            } else {
                String telegramId = String.valueOf(update.getCallbackQuery().getFrom().getId());
                String userName = update.getCallbackQuery().getFrom().getFirstName();
                ticketProcessingService.checkExam(bot, telegramId, userName, chatId);
                return true;
            }
        }
        return false;
    }
}