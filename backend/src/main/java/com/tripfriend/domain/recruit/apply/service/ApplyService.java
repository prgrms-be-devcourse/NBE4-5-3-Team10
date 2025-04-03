package com.tripfriend.domain.recruit.apply.service;

import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.repository.MemberRepository;
import com.tripfriend.domain.member.member.service.AuthService;
import com.tripfriend.domain.recruit.apply.dto.ApplyCreateRequestDto;
import com.tripfriend.domain.recruit.apply.dto.ApplyResponseDto;
import com.tripfriend.domain.recruit.apply.entity.Apply;
import com.tripfriend.domain.recruit.apply.repository.ApplyRepository;
import com.tripfriend.domain.recruit.recruit.entity.Recruit;
import com.tripfriend.domain.recruit.recruit.repository.RecruitRepository;
import com.tripfriend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplyService {
    private final ApplyRepository applyRepository;
    private final RecruitRepository recruitRepository;
    private final MemberRepository memberRepository;
    private final AuthService authService;

    /**
     * 현재 로그인한 회원객체를 반환하는 메서드
     *
     * @param token JWT 토큰
     * @return 로그인한 회원 객체
     * @throws ServiceException 로그인하지 않은 경우 예외 발생
     */
    public Member getLoggedInMember(String token) {
        // 로그인 여부 확인 및 회원 정보 가져오기
        Member member = authService.getLoggedInMember(token);

        if (member == null) {
            throw new ServiceException("401-1", "로그인이 필요합니다.");
        }

        return member;
    }

    @Transactional
    public List<ApplyResponseDto> findByRecruitId(Long recruitId) {
        Recruit recruit = recruitRepository.findById(recruitId).orElseThrow(() -> new ServiceException("404-3", "해당 모집글이 존재하지 않습니다."));
        return recruit.getApplies().stream()
                .map(ApplyResponseDto::new)
                .toList();
    }

    @Transactional
    public ApplyResponseDto create(Long recruitId, ApplyCreateRequestDto requestDto, String token) {
        Member member = getLoggedInMember(token);
        Recruit recruit = recruitRepository.findById(recruitId).orElseThrow(() -> new ServiceException("404-3", "해당 모집글이 존재하지 않습니다."));
        return new ApplyResponseDto(applyRepository.save(requestDto.toEntity(member, recruit)));
    }

    public void delete(Long applyId, String token) {
        Apply apply = applyRepository.findById(applyId).orElseThrow(() -> new ServiceException("404-3", "해당 모집 댓글이 존재하지 않습니다."));
        Member member = getLoggedInMember(token);

        // 본인 확인
        if (!apply.getMember().getId().equals(member.getId()) && !member.getAuthority().equals("ADMIN")) {
            throw new ServiceException("403-2", "관리자가 아니라면 본인이 등록한 동행 요청 댓글만 삭제할 수 있습니다.");
        }
        applyRepository.deleteById(applyId);
    }
}
