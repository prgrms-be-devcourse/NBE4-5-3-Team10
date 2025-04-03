package com.tripfriend.domain.qna.dto;

import com.tripfriend.domain.qna.entity.Question;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuestionDto {
    private Long id;
    private String title;
    private String content;
    private String createdAt;
    private String updatedAt;
    private String memberUsername;



    public QuestionDto(Question question) {
        this.id = question.getId();
        this.title = question.getTitle();
        this.content = question.getContent();
        this.createdAt = question.getCreatedAt().toString();
        this.updatedAt = question.getUpdatedAt().toString();
        this.memberUsername = question.getMember().getUsername();
    }
}
