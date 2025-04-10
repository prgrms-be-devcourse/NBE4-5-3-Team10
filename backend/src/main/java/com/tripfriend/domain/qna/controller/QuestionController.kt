package com.tripfriend.domain.qna.controller;

import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.service.AuthService;
import com.tripfriend.domain.qna.dto.QuestionDto;
import com.tripfriend.domain.qna.entity.Question;
import com.tripfriend.domain.qna.repository.QuestionRepository;
import com.tripfriend.domain.qna.service.QuestionService;
import com.tripfriend.global.annotation.CheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/qna")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;
    private final AuthService authService;


    //전체 질문 조회
    @Tag(name = "질문 API", description = "QnA 질문 관련 기능을 제공합니다.")
    @Operation(summary = "질문 목록 조회", description = "등록된 모든 QnA 질문 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<QuestionDto>> getAllQuestions() {
        List<Question> questions = questionService.getAllQuestions();
        List<QuestionDto> responseDtos = questions.stream()
                .map(QuestionDto::new)
                .toList();
        return ResponseEntity.ok(responseDtos);
    }


    // 질문 검색
    @Operation(summary = "질문 상세 조회", description = "ID에 해당하는 QnA 질문의 상세 내용을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<QuestionDto> getQuestionById(@PathVariable("id") Long id) {
        Question question = questionService.getQuestionById(id);
        return ResponseEntity.ok(new QuestionDto(question));
    }

    //질문 삭제
    @Operation(summary = "질문 삭제", description = "로그인한 사용자가 자신이 작성한 질문을 삭제합니다.")
    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable("questionId") Long questionId,
            @RequestHeader("Authorization") String token
    ) {
        Member member = authService.getLoggedInMember(token);
        questionService.deleteQuestionById(questionId, member);
        return ResponseEntity.noContent().build();
    }

    //질문생성
    @Operation(summary = "질문 작성", description = "로그인한 사용자가 QnA 질문을 등록합니다.")
    @PostMapping("/question")
    public ResponseEntity<Void> createQuestion(
            @RequestBody QuestionDto requestDto,
            @RequestHeader("Authorization") String token
    ) {
        Member member = authService.getLoggedInMember(token);
        questionService.createQuestion(requestDto.getTitle(), requestDto.getContent(), member);
        return ResponseEntity.ok().build();
    }


}
