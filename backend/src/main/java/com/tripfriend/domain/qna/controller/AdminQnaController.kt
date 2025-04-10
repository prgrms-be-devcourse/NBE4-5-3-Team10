package com.tripfriend.domain.qna.controller;

import com.tripfriend.domain.qna.dto.QuestionDto;
import com.tripfriend.domain.qna.dto.QuestionWithAnswersDto;
import com.tripfriend.domain.qna.service.AnswerService;
import com.tripfriend.domain.qna.service.QuestionService;
import com.tripfriend.global.annotation.CheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "관리자 QnA API", description = "관리자가 QnA 질문 및 답변을 관리할 수 있는 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/qna")
public class AdminQnaController {

    private final QuestionService questionService;
    private final AnswerService answerService;

    // 관리자용 QnA 목록 조회
    @Operation(summary = "관리자 QnA 목록 조회", description = "사용자의 QnA 목록을 조회합니다.")
    @CheckPermission("ADMIN")
    @GetMapping("/questions")
    public ResponseEntity<List<QuestionDto>> getAllQuestionsForAdmin() {
        List<QuestionDto> questions = questionService.getAllQuestionsForAdmin();
        return ResponseEntity.ok(questions);
    }

    // 관리자용 QnA 삭제
    @CheckPermission("ADMIN")
    @Operation(summary = "관리자 질문 삭제", description = "QnA 모든 질문을 삭제할 수 있습니다.")
    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable("id") Long id) {
        questionService.deleteQuestionByAdmin(id);
        return ResponseEntity.noContent().build();
    }
    // 관리자용 답변 삭제
    @CheckPermission("ADMIN")
    @Operation(summary = "관리자 답변 삭제", description = "QnA 모든 답변을 삭제할 수 있습니다.")
    @DeleteMapping("/answers/{id}")
    public ResponseEntity<Void> deleteAnswerByAdmin(@PathVariable("id") Long id) {
        answerService.deleteAnswerByAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @CheckPermission("ADMIN")
    @Operation(summary = "관리자 특정 질문에 대한 답변 제공", description = "QnA 특정 질문 ID에 대한 질문 내용과 답변을 내려줍니다.")
    @GetMapping("/questions/{id}")
    public ResponseEntity<QuestionWithAnswersDto> getQuestionWithAnswers(@PathVariable("id") Long id) {
        QuestionWithAnswersDto dto = questionService.getQuestionWithAnswers(id);
        return ResponseEntity.ok(dto);
    }



}
