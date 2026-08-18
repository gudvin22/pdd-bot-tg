package com.pdd.pddbottg.service;

import com.pdd.pddbottg.dto.AiAnalysisTicketRequestDto;
import com.pdd.pddbottg.dto.WrongAnswerDto;
import com.pdd.pddbottg.entity.ExamSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiTicketAnalysisService {

    public AiAnalysisTicketRequestDto buildRequestDto(ExamSession examSession) {
        AiAnalysisTicketRequestDto request =  new AiAnalysisTicketRequestDto();
        request.setTicketNumber(examSession.getTicketNumber());
        List<WrongAnswerDto> wrongs = examSession.getWrongAnswers();
        List<AiAnalysisTicketRequestDto.ErrorDetail> details = new ArrayList<>();

        if(wrongs != null){
            for(WrongAnswerDto wrong : wrongs){
                AiAnalysisTicketRequestDto.ErrorDetail detail = new AiAnalysisTicketRequestDto.ErrorDetail();
                detail.setQuestionNumber(wrong.getQuestionNumber());
                detail.setUserAnswerIndex(wrong.getUserAnswerIndex());
                detail.setCorrectAnswerIndex(wrong.getCorrectAnswerIndex());
                details.add(detail);
            }
        }

        request.setErrors(details);
        return request;
    }
}
