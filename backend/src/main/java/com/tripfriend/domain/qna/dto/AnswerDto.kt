package com.tripfriend.domain.qna.dto;

import com.tripfriend.domain.qna.entity.Answer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnswerDto {
    private Long answerId;
    private String content;
    private String memberUsername;
    private String createdAt;

    public AnswerDto(Answer answer) {
        this.answerId = answer.getId();
        this.content = answer.getContent();
        this.memberUsername = answer.getMember().getUsername();
        this.createdAt = answer.getCreatedAt().toString();
    }
}
