package com.pdd.pddbottg.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExamResultDto {
    private boolean passed;
    private String message;
    private List<WrongAnswerDto> wrongAnswers;
    private List<QuestionDto> additionalQuestions; // для дополнительных вопросов
}