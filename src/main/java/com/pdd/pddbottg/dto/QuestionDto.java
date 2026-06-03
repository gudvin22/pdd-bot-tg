package com.pdd.pddbottg.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionDto {
    private int questionNumber;
    private String questionText;
    private List<String> answersText;
    private String imageUrlSmall;

}
