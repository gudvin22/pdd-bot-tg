package com.pdd.pddbottg.entity;

import com.pdd.pddbottg.dto.QuestionDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExamSession {
    private int ticketNumber;
    private List<QuestionDto> questions;
    private List<Integer> userAnswers;
    private int currentQuestionIndex;

    public ExamSession(int ticketNumber, List<QuestionDto> questions) {
        this.ticketNumber = ticketNumber;
        this.questions = questions;
        this.currentQuestionIndex = 0;
        this.userAnswers = new ArrayList<>(questions.size());
        for (int i = 0; i < questions.size(); i++) {
            this.userAnswers.add(-1);
        }
    }


}
