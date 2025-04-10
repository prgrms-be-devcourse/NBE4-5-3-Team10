package com.tripfriend.domain.qna.service;

import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.repository.MemberRepository;
import com.tripfriend.domain.qna.dto.QuestionDto;
import com.tripfriend.domain.qna.dto.QuestionWithAnswersDto;
import com.tripfriend.domain.qna.entity.Question;
import com.tripfriend.domain.qna.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final MemberRepository memberRepository;

    //질문 생성
    public Question createQuestion(String title, String content, Member member) {
        Question question = Question.builder()
                .title(title)
                .content(content)
                .member(member)
                .build();
        return questionRepository.save(question);
    }

    //전체 질문 조회
    public List<Question> getAllQuestions() {
        return questionRepository.findAllWithMember();
    }

    //특정 질문 조회
    public Question getQuestionById(Long id) {
        return questionRepository.findByIdWithMember(id)
                .orElseThrow(() -> new RuntimeException("해당 질문을 찾을 수 없습니다."));
    }

    //질문 삭제
    public void deleteQuestionById(Long id, Member member) {
        Question question = getQuestionById(id);

        // 작성자인지 확인
        if (!question.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("작성자만 질문을 삭제할 수 있습니다.");
        }

        questionRepository.delete(question);
    }

    // 관리자용 전체 질문 조회
    public List<QuestionDto> getAllQuestionsForAdmin() {
        return questionRepository.findAllWithMember()
                .stream()
                .map(QuestionDto::new)
                .collect(Collectors.toList());
    }


    // 관리자용 질문 삭제
    @Transactional
    public void deleteQuestionByAdmin(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 질문을 찾을 수 없습니다."));
        questionRepository.delete(question);
    }

    @Transactional
    public QuestionWithAnswersDto getQuestionWithAnswers(Long id) {
        Question question = questionRepository.findByIdWithAnswers(id)
                .orElseThrow(() -> new RuntimeException("해당 질문을 찾을 수 없습니다."));

        return new QuestionWithAnswersDto(question);
    }


}
