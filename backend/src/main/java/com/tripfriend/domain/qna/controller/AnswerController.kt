package com.tripfriend.domain.qna.controller

import com.tripfriend.domain.member.member.service.AuthService
import com.tripfriend.domain.qna.dto.AnswerDto
import com.tripfriend.domain.qna.qna.service.AnswerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@Tag(name = "답변 API", description = "QnA에 대한 답변 기능을 제공합니다.")
@RestController
@RequestMapping("/qna")
class AnswerController(
    private val answerService: AnswerService,
    private val authService: AuthService
) {

    //로그인 유저 정보조회
    @Operation(summary = "현재 로그인한 사용자 정보 조회", description = "AccessToken을 통해 로그인된 사용자의 ID와 username을 조회합니다.")
    @GetMapping("/me")
    fun getCurrentUser(@RequestHeader("Authorization") token: String): ResponseEntity<Map<String, Any?>> {
        val member = authService.getLoggedInMember(token)
        val response = mapOf(
            "id" to member.id,
            "username" to member.username
        )
        return ResponseEntity.ok(response)
    }

    //답변 생성
    @Operation(summary = "답변 등록", description = "질문 ID에 해당하는 질문에 대해 답변을 등록합니다.")
    @PostMapping("/{questionId}/answer")
    fun createAnswer(
        @PathVariable questionId: Long,
        @RequestBody request: Map<String, String>,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Void> {
        val content = request["content"] ?: throw IllegalArgumentException("답변 내용이 필요합니다.")
        val member = authService.getLoggedInMember(token)
        answerService.createAnswer(questionId, member.id!!, content)
        return ResponseEntity.ok().build()
    }

    // 답변 목록 조회
    @Operation(summary = "답변 목록 조회", description = "질문 ID에 대한 모든 답변을 조회합니다.")
    @GetMapping("/{questionId}/answers")
    fun getAnswers(@PathVariable questionId: Long): ResponseEntity<List<AnswerDto>> {
        val answers = answerService.getAnswersByQuestionId(questionId)
        return ResponseEntity.ok(answers)
    }

    //답변 삭제
    @Operation(summary = "답변 삭제", description = "본인이 작성한 답변을 삭제합니다.")
    @DeleteMapping("/answer/{answerId}")
    fun deleteAnswer(
        @PathVariable answerId: Long,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Void> {
        val member = authService.getLoggedInMember(token)
        answerService.deleteAnswer(answerId, member)
        return ResponseEntity.noContent().build()
    }
}
