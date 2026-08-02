package com.pdd.pddbottg.handlers;


import com.pdd.pddbottg.PddBot;
import com.pdd.pddbottg.entity.ExamSession;
import com.pdd.pddbottg.service.KeyboardService;
import com.pdd.pddbottg.service.MessageSender;
import com.pdd.pddbottg.service.SessionStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import com.pdd.pddbottg.dto.QuestionDto;
import com.pdd.pddbottg.dto.WrongAnswerDto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.List;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class ErrorViewHandler implements UpdateHandler{

    private final SessionStorage sessionStorage;
    private final MessageSender messageSender;
    private final KeyboardService keyboardService;

    @Value("${bot.message.responseRandomTicketErrorSession}")
    private String responseRandomTicketErrorSession;

    @Override
    public boolean handle(PddBot bot, Update update) {

        if(update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            if(!callbackData.startsWith("view_errors") && !callbackData.startsWith("error_") && !callbackData.startsWith("get_ai_analysis")) {
                return false;
            }

            ExamSession session = sessionStorage.getSession(chatId);
            if(session == null) {
                messageSender.sendMessageWithReplyKeyboard(bot, chatId, responseRandomTicketErrorSession, keyboardService.mainMenu());
                return true;
            }

            switch (callbackData)
                {
                case "view_errors": {
                    //messageSender.sendMessage(bot, chatId,"Показываем ошибки");
                    showError(bot, chatId, session, 0);
                    break;
                }
                case "error_exit": {
                    sessionStorage.removeSession(chatId);
                    messageSender.sendMessageWithReplyKeyboard(bot, chatId, "Вы вернулись в главное меню", keyboardService.mainMenu());
                    break;
                }
                case "error_next": {
                    int current = session.getCurrentErrorIndex();
                    if (current < session.getWrongAnswers().size() - 1) {
                        showError(bot, chatId, session, current + 1);
                    }
                    break;
                }
                case "get_ai_analysis": {
                    messageSender.sendMessage(bot, chatId,"AI анализ в разработке");
                    sessionStorage.removeSession(chatId);
                    break;
                }

                }

        }

        return false;
    }

    private void showError(PddBot bot, Long chatId, ExamSession session, int index) {
        List<WrongAnswerDto> wrongAnswers = session.getWrongAnswers();
        if(wrongAnswers == null || wrongAnswers.isEmpty() || index >= wrongAnswers.size()) {
            //messageSender.sendMessage(bot, chatId, "Ошибок нет.");
            sessionStorage.removeSession(chatId);
            return;
        }

        session.setCurrentErrorIndex(index);
        WrongAnswerDto error = wrongAnswers.get(index);

        //Найти вопрос
        QuestionDto question = session.getQuestions().stream()
                .filter(q -> q.getQuestionNumber() == error.getQuestionNumber())
                .findFirst()
                .orElse(null);
        if(question == null) {
            messageSender.sendMessage(bot, chatId, "Вопрос не найден");
            return;
        }

        //текст
        StringBuilder text = new StringBuilder();
        text.append("Ошибка ").append(index + 1).append(" из ")
                .append(wrongAnswers.size()).append("\n");
        text.append("Вопрос № ").append(error.getQuestionNumber()).append("\n\n");
        text.append("<b>");
        text.append(question.getQuestionText()).append("</b>\n");

        List<String> answers = question.getAnswersText();
        for (int j = 0; j < answers.size(); j++) {
            String prefix = "";
            if (j == error.getCorrectAnswerIndex()) {
                prefix = "✅ ";
            }
            if (j == error.getUserAnswerIndex() && j != error.getCorrectAnswerIndex()) {
                prefix = "❌ ";
            }
            text.append(prefix).append(j + 1).append(". ").append(answers.get(j)).append("\n");
        }
        text.append("\n<b>Объяснение:</b>\n").append(error.getExplanation());

        //клавиатура
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        if (index < wrongAnswers.size() - 1) {
            InlineKeyboardButton next = new InlineKeyboardButton();
            next.setText("Дальше ▶️");
            next.setCallbackData("error_next");
            row.add(next);
        } else {
            InlineKeyboardButton ai = new InlineKeyboardButton();
            ai.setText("🧠 Получить AI анализ");
            ai.setCallbackData("get_ai_analysis");
            row.add(ai);
        }

        rows.add(row);
        keyboard.setKeyboard(rows);

        // Картинка
        String imageUrl = question.getImageUrlSmall();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                messageSender.sendFoto(bot, chatId, imageUrl);
            } catch (Exception e) {
                // игнорируем
            }
        }



        messageSender.sendMessageInlineKeyboard(bot, chatId, text.toString(), keyboard);



    }
}
