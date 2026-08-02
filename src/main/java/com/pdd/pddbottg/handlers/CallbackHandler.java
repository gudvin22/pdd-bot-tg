package com.pdd.pddbottg.handlers;

import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.dto.QuestionDto;
import com.pdd.pddbottg.entity.ExamSession;
import com.pdd.pddbottg.service.KeyboardService;
import com.pdd.pddbottg.service.MessageSender;
import com.pdd.pddbottg.service.SessionStorage;
import com.pdd.pddbottg.service.TicketProcessingService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class CallbackHandler implements UpdateHandler{
    private final SessionStorage sessionStorage;
    private final MessageSender messageSender;
    private final TicketProcessingService ticketProcessingService;
    private final KeyboardService keyboardService;

    @Value("${bot.message.responseRandomQuestion}")
    private String responseMessage;

    @Value("${bot.message.responseRandomTicketErrorSession}")
    private String responseRandomTicketErrorSession;

    @Override
    public boolean handle(PddBot bot, Update update) {

        if(update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callbackData = update.getCallbackQuery().getData();



            ExamSession session = sessionStorage.getSession(chatId);
            if(session == null) {
                messageSender.sendMessageWithReplyKeyboard(bot, chatId, responseRandomTicketErrorSession, keyboardService.mainMenu());
                return true;
            }

            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(update.getCallbackQuery().getId());
            answer.setText(responseMessage);
            try {
                bot.execute(answer);
            }  catch (Exception e) {
                messageSender.sendMessage(bot, chatId, "Не удалось отправить ответ. Ошибка: " + e.getMessage());
            }

            // Проверяем, что это ответ на вопрос (начинается с "answer_")
            if (!callbackData.startsWith("answer_")) {
                return false;
            }

            int userAnswerIndex = Integer.parseInt(callbackData.substring(7));
            session.getUserAnswers().set(session.getCurrentQuestionIndex(),  userAnswerIndex);
            session.setCurrentQuestionIndex(session.getCurrentQuestionIndex()+1);

            if(session.getCurrentQuestionIndex() < session.getQuestions().size()) {
                QuestionDto question = session.getQuestions().get(session.getCurrentQuestionIndex());
                String imageUrl = question.getImageUrlSmall();

                messageSender.sendMessage(bot, chatId, "Вопрос № " + (session.getCurrentQuestionIndex() + 1));

                try {
                    if(imageUrl != null && !imageUrl.isEmpty()) {
                        messageSender.sendFoto(bot, chatId, imageUrl);
                    }

                    //messageSender.sendQuestionInline(bot, chatId, question.getQuestionText(), question.getAnswersText());
                    messageSender.sendQuestionTextWithInline(bot, chatId, question.getQuestionText(), question.getAnswersText());


                } catch (Exception e) {
                    messageSender.sendMessage(bot, chatId, "Не удалось загрузить билет. Ошибка: " + e.getMessage());
                }

            } else {
                //после последнего вопроса завершаем
                String telegramId = String.valueOf(update.getCallbackQuery().getFrom().getId());
                String userName = update.getCallbackQuery().getFrom().getFirstName();
                ticketProcessingService.checkExam(bot, telegramId,userName,chatId);
                //sessionStorage.removeSession(chatId);

                return true;

            }

        }
        return false;
    }
}
