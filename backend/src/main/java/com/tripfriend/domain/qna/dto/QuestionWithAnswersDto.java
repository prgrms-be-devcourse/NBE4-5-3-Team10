package com.tripfriend.domain.qna.dto;

import com.tripfriend.domain.qna.entity.Answer;
import com.tripfriend.domain.qna.entity.Question;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class QuestionWithAnswersDto {

    private Long id;
    private String title;
    private String content;
    private String createdAt;
    private String updatedAt;
    private String memberUsername;
    private List<AnswerDto> answers;

    public QuestionWithAnswersDto(Question question) {
        this.id = question.getId();
        this.title = question.getTitle();
        this.content = question.getContent();
        this.createdAt = question.getCreatedAt().toString();
        this.updatedAt = question.getUpdatedAt().toString();
        this.memberUsername = question.getMember().getUsername();
        this.answers = question.getAnswers()
                .stream()
                .map(AnswerDto::new)
                .collect(Collectors.toList());
    }
}
