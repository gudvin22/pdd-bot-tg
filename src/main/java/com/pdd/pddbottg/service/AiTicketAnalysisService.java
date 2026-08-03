package com.pdd.pddbottg.service;

import com.pdd.pddbottg.dto.AiAnalysisRequestDto;
import com.pdd.pddbottg.dto.WrongAnswerDto;
import com.pdd.pddbottg.entity.ExamSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiTicketAnalysisService {

    public AiAnalysisRequestDto buildRequestDto(ExamSession examSession) {
        AiAnalysisRequestDto request =  new AiAnalysisRequestDto();
        request.setTicketNumber(examSession.getTicketNumber());
        List<WrongAnswerDto> wrongs = examSession.getWrongAnswers();
        List<AiAnalysisRequestDto.ErrorDetail> details = new ArrayList<>();

        if(wrongs != null){
            for(WrongAnswerDto wrong : wrongs){
                AiAnalysisRequestDto.ErrorDetail detail = new AiAnalysisRequestDto.ErrorDetail();
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
