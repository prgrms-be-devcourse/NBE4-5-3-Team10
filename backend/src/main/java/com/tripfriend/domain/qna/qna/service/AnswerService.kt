package com.tripfriend.domain.qna.service;

import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.repository.MemberRepository;
import com.tripfriend.domain.qna.dto.AnswerDto;
import com.tripfriend.domain.qna.entity.Answer;
import com.tripfriend.domain.qna.entity.Question;
import com.tripfriend.domain.qna.repository.AnswerRepository;
import com.tripfriend.domain.qna.repository.QuestionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;
    // private final QuestionService questionService;
    //private final MemberService memberService;
    private final QuestionRepository questionRepository;
    private final MemberRepository memberRepository;


    //답변 생성
    @Transactional
    public void createAnswer(Long questionId, Long memberId, String content) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        Answer answer = Answer.builder()
                .question(question)
                .member(member)
                .content(content)
                .build();

        answerRepository.save(answer);

    }
    @Transactional
    public List<AnswerDto> getAnswersByQuestionId(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다."));

        return question.getAnswers().stream()
                .map(AnswerDto::new)
                .toList();
    }


    //답변 삭제
    @Transactional
    public void deleteAnswer(Long answerId, Member currentMember) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("답변을 찾을 수 없습니다."));

        if (!answer.getMember().getId().equals(currentMember.getId())) {
            throw new SecurityException("본인의 답변만 삭제할 수 있습니다.");
        }

        answerRepository.delete(answer);
    }

    //관리자 답변 삭제
    @Transactional
    public void deleteAnswerByAdmin(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("해당 답변을 찾을 수 없습니다."));
        answerRepository.delete(answer);
    }

}
