package com.tripfriend.domain.qna.controller;

import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.service.AuthService;
import com.tripfriend.domain.qna.dto.AnswerDto;
import com.tripfriend.domain.qna.service.AnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "답변 API", description = "QnA에 대한 답변 기능을 제공합니다.")
@RestController
@RequestMapping("/qna")
@RequiredArgsConstructor
public class AnswerController {
    private final AnswerService answerService;
    private final AuthService authService;

    //로그인 유저 정보조회
    @Operation(summary = "현재 로그인한 사용자 정보 조회", description = "AccessToken을 통해 로그인된 사용자의 ID와 username을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String token) {
        Member member = authService.getLoggedInMember(token);

        Map<String, Object> response = new HashMap<>();
        response.put("id", member.getId());
        response.put("username", member.getUsername());

        return ResponseEntity.ok(response);
    }

    //답변 생성
    @Operation(summary = "답변 등록", description = "질문 ID에 해당하는 질문에 대해 답변을 등록합니다.")
    @PostMapping("/{questionId}/answer")
    public ResponseEntity<Void> createAnswer(
            @PathVariable("questionId")  Long questionId,
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String token
    ) {
        String content = request.get("content");
        Member member = authService.getLoggedInMember(token); // accessToken에서 member 추출
        answerService.createAnswer(questionId, member.getId(), content);
        return ResponseEntity.ok().build();
    }

    // 답변 목록 조회
    @Operation(summary = "답변 목록 조회", description = "질문 ID에 대한 모든 답변을 조회합니다.")
    @GetMapping("/{questionId}/answers")
    public ResponseEntity<List<AnswerDto>> getAnswers(@PathVariable("questionId") Long questionId) {
        return ResponseEntity.ok(answerService.getAnswersByQuestionId(questionId));
    }

    //답변 삭제
    @Operation(summary = "답변 삭제", description = "본인이 작성한 답변을 삭제합니다.")
    @DeleteMapping("/answer/{answerId}")
    public ResponseEntity<Void> deleteAnswer(
            @PathVariable("answerId") Long answerId,
            @RequestHeader("Authorization") String token) {

        Member member = authService.getLoggedInMember(token); // 토큰 기반 사용자 조회
        answerService.deleteAnswer(answerId, member);
        return ResponseEntity.noContent().build();
    }


}
