package com.tripfriend.domain.qna.controller

import com.tripfriend.domain.qna.dto.QuestionDto
import com.tripfriend.domain.qna.dto.QuestionWithAnswersDto
import com.tripfriend.domain.qna.qna.service.AnswerService
import com.tripfriend.domain.qna.qna.service.QuestionService
import com.tripfriend.global.annotation.CheckPermission
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "관리자 QnA API", description = "관리자가 QnA 질문 및 답변을 관리할 수 있는 기능을 제공합니다.")
@RestController
@RequestMapping("/admin/qna")
class AdminQnaController(
    private val questionService: QuestionService,
    private val answerService: AnswerService
) {

    @GetMapping("/questions")
    @CheckPermission("ADMIN")
    @Operation(summary = "관리자 QnA 목록 조회", description = "사용자의 QnA 목록을 조회합니다.")
    fun getAllQuestionsForAdmin(): ResponseEntity<List<QuestionDto>> {
        val questions = questionService.getAllQuestionsForAdmin()
        return ResponseEntity.ok(questions)
    }

    // 관리자용 QnA 삭제
    @DeleteMapping("/questions/{id}")
    @CheckPermission("ADMIN")
    @Operation(summary = "관리자 질문 삭제", description = "QnA 모든 질문을 삭제할 수 있습니다.")
    fun deleteQuestion(@PathVariable id: Long): ResponseEntity<Void> {
        questionService.deleteQuestionByAdmin(id)
        return ResponseEntity.noContent().build()
    }

    // 관리자용 답변 삭제
    @DeleteMapping("/answers/{id}")
    @CheckPermission("ADMIN")
    @Operation(summary = "관리자 답변 삭제", description = "QnA 모든 답변을 삭제할 수 있습니다.")
    fun deleteAnswerByAdmin(@PathVariable id: Long): ResponseEntity<Void> {
        answerService.deleteAnswerByAdmin(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/questions/{id}")
    @CheckPermission("ADMIN")
    @Operation(summary = "관리자 특정 질문에 대한 답변 제공", description = "QnA 특정 질문 ID에 대한 질문 내용과 답변을 내려줍니다.")
    fun getQuestionWithAnswers(@PathVariable id: Long): ResponseEntity<QuestionWithAnswersDto> {
        val dto = questionService.getQuestionWithAnswers(id)
        return ResponseEntity.ok(dto)
    }
}
