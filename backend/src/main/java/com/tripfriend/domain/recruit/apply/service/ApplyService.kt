package com.tripfriend.domain.recruit.apply.service

import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.member.member.repository.MemberRepository
import com.tripfriend.domain.member.member.service.AuthService
import com.tripfriend.domain.recruit.apply.dto.ApplyCreateRequestDto
import com.tripfriend.domain.recruit.apply.dto.ApplyResponseDto
import com.tripfriend.domain.recruit.apply.repository.ApplyRepository
import com.tripfriend.domain.recruit.recruit.repository.RecruitRepository
import com.tripfriend.global.exception.ServiceException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ApplyService (
    private val applyRepository: ApplyRepository,
    private val recruitRepository: RecruitRepository,
    private val memberRepository: MemberRepository,
    private val authService: AuthService){


    /**
     * 현재 로그인한 회원객체를 반환하는 메서드
     *
     * @param token JWT 토큰
     * @return 로그인한 회원 객체
     * @throws ServiceException 로그인하지 않은 경우 예외 발생
     */
    fun getLoggedInMember(token: String): Member {
        // 로그인 여부 확인 및 회원 정보 가져오기
        val member = authService.getLoggedInMember(token)
            ?: throw ServiceException("401-1", "로그인이 필요합니다.")

        return member
    }

    @Transactional
    fun findByRecruitId(recruitId: Long): List<ApplyResponseDto> {
        val recruit = recruitRepository.findById(recruitId).orElseThrow {
            ServiceException(
                "404-3",
                "해당 모집글이 존재하지 않습니다."
            )
        }
        return recruit.applies.map { ApplyResponseDto(it) }
    }

    @Transactional
    fun create(recruitId: Long, requestDto: ApplyCreateRequestDto, token: String): ApplyResponseDto {
        val member = getLoggedInMember(token)
        val recruit = recruitRepository.findById(recruitId).orElseThrow {
            ServiceException(
                "404-3",
                "해당 모집글이 존재하지 않습니다."
            )
        }
        return ApplyResponseDto(applyRepository.save(requestDto.toEntity(member, recruit)))
    }

    fun delete(applyId: Long, token: String) {
        val apply = applyRepository.findById(applyId).orElseThrow {
            ServiceException(
                "404-3",
                "해당 모집 댓글이 존재하지 않습니다."
            )
        }
        val member = getLoggedInMember(token)

        // 본인 확인
        if (apply.member.id != member.id && member.authority != "ADMIN") {
            throw ServiceException("403-2", "관리자가 아니라면 본인이 등록한 동행 요청 댓글만 삭제할 수 있습니다.")
        }
        applyRepository.deleteById(applyId)
    }
}
