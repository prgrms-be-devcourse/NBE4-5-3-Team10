package com.tripfriend.domain.qna.controller

import com.tripfriend.domain.member.member.service.AuthService
import com.tripfriend.domain.qna.dto.QuestionDto
import com.tripfriend.domain.qna.entity.Question
import com.tripfriend.domain.qna.qna.service.QuestionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "질문 API", description = "QnA 질문 관련 기능을 제공합니다.")
@RestController
@RequestMapping("/qna")
class QuestionController(
    private val questionService: QuestionService,
    private val authService: AuthService
) {


    @Operation(summary = "질문 목록 조회", description = "등록된 모든 QnA 질문 목록을 조회합니다.")
    @GetMapping
    fun getAllQuestions(): ResponseEntity<List<QuestionDto>> {
        val questions: List<Question> = questionService.getAllQuestions()
        val responseDtos = questions.map { QuestionDto(it) }
        return ResponseEntity.ok(responseDtos)
    }



    // 질문 검색
    @Operation(summary = "질문 상세 조회", description = "ID에 해당하는 QnA 질문의 상세 내용을 조회합니다.")
    @GetMapping("/{id}")
    fun getQuestionById(@PathVariable id: Long): ResponseEntity<QuestionDto> {
        val question = questionService.getQuestionById(id)
        return ResponseEntity.ok(QuestionDto(question))
    }

    //질문 삭제
    @Operation(summary = "질문 삭제", description = "로그인한 사용자가 자신이 작성한 질문을 삭제합니다.")
    @DeleteMapping("/{questionId}")
    fun deleteQuestion(
        @PathVariable questionId: Long,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Void> {
        val member = authService.getLoggedInMember(token)
        questionService.deleteQuestionById(questionId, member)
        return ResponseEntity.noContent().build()
    }


    //질문생성
    @Operation(summary = "질문 작성", description = "로그인한 사용자가 QnA 질문을 등록합니다.")
    @PostMapping("/question")
    fun createQuestion(
        @RequestBody requestDto: QuestionDto,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Void> {
        val member = authService.getLoggedInMember(token)
        questionService.createQuestion(requestDto.title, requestDto.content, member)
        return ResponseEntity.ok().build()
    }
}
