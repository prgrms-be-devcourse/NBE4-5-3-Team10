package com.tripfriend.domain.member.member.service

import com.tripfriend.domain.member.member.dto.JoinRequestDto
import com.tripfriend.domain.member.member.dto.MemberResponseDto
import com.tripfriend.domain.member.member.dto.MemberUpdateRequestDto
import com.tripfriend.domain.member.member.repository.MemberRepository
import com.tripfriend.global.util.ImageUtil
import jakarta.mail.MessagingException
import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.time.LocalDateTime

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val authService: AuthService,
    private val mailService: MailService,
    private val imageUtil: ImageUtil,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    @Throws(MessagingException::class)
    fun join(joinRequestDto: JoinRequestDto): MemberResponseDto {

        val email = joinRequestDto.email

        // 중복 검사
        if (memberRepository.existsByUsername(joinRequestDto.username)) {
            throw RuntimeException("이미 사용 중인 아이디입니다.")
        }
        if (memberRepository.existsByEmail(joinRequestDto.email)) {
            throw RuntimeException("이미 사용 중인 이메일입니다.")
        }
        if (memberRepository.existsByNickname(joinRequestDto.nickname)) {
            throw RuntimeException("이미 사용 중인 닉네임입니다.")
        }

        // 비밀번호 암호화
        val encryptedPassword = passwordEncoder.encode(joinRequestDto.password)

        // DTO를 엔티티로 변환하고 암호화된 비밀번호를 설정
        val member = joinRequestDto.toEntity()
        member.password = encryptedPassword
        val savedMember = memberRepository.save(member)

        return MemberResponseDto.fromEntity(savedMember)
    }

    @Transactional
    fun updateMember(id: Long, memberUpdateRequestDto: MemberUpdateRequestDto): MemberResponseDto {

        val member = memberRepository.findById(id)
            .orElseThrow { RuntimeException("존재하지 않는 회원입니다.") }

        // 이메일 중복 검사 (변경된 경우에만)
        if (memberUpdateRequestDto.email != null && memberUpdateRequestDto.email != member.email) {
            if (memberRepository.existsByEmail(memberUpdateRequestDto.email!!)) {
                throw RuntimeException("이미 사용 중인 이메일입니다.")
            }
            member.email = memberUpdateRequestDto.email!!
        }

        // 닉네임 중복 검사 (변경된 경우에만)
        if (memberUpdateRequestDto.nickname != null && memberUpdateRequestDto.nickname != member.nickname) {
            if (memberRepository.existsByNickname(memberUpdateRequestDto.nickname!!)) {
                throw RuntimeException("이미 사용 중인 닉네임입니다.")
            }
            member.nickname = memberUpdateRequestDto.nickname!!
        }

        // 비밀번호 변경 (값이 있는 경우만)
        if (!memberUpdateRequestDto.password.isNullOrEmpty()) {
            member.password = memberUpdateRequestDto.password!!
        }

        // 나머지 필드 업데이트 (null이 아닌 경우만)
        memberUpdateRequestDto.profileImage?.let { member.profileImage = it }
        memberUpdateRequestDto.gender?.let { member.gender = it }
        memberUpdateRequestDto.ageRange?.let { member.ageRange = it }
        memberUpdateRequestDto.travelStyle?.let { member.travelStyle = it }
        memberUpdateRequestDto.aboutMe?.let { member.aboutMe = it }

        member.updatedAt = LocalDateTime.now()
        val updatedMember = memberRepository.save(member)

        return MemberResponseDto.fromEntity(updatedMember)
    }

    @Transactional
    fun deleteMember(id: Long, request: HttpServletRequest, response: HttpServletResponse) {

        val member = memberRepository.findById(id)
            .orElseThrow { RuntimeException("존재하지 않는 회원입니다.") }

        authService.logout(request, response)

        member.deleted = true
        member.deletedAt = LocalDateTime.now()
        memberRepository.save(member)
    }

    @Transactional
    fun restoreMember(id: Long) {
        val member = memberRepository.findByIdAndDeletedTrue(id)
            .orElseThrow { RuntimeException("존재하지 않거나 이미 활성화된 회원입니다.") }

        // 복구 가능 기간 확인
        if (!member.canBeRestored()) {
            throw RuntimeException("계정 복구 기간이 만료되었습니다.")
        }

        member.deleted = false
        member.deletedAt = null
        memberRepository.save(member)
    }

    fun getMyPage(id: Long, username: String): MemberResponseDto {

        val member = memberRepository.findById(id)
            .orElseThrow { UsernameNotFoundException("존재하지 않는 회원입니다.") }

        // 보안 강화: 본인의 정보만 조회할 수 있도록 체크
        if (member.username != username) {
            throw AccessDeniedException("본인만 확인할 수 있습니다.")
        }

        return MemberResponseDto.fromEntity(member)
    }

    // 회원 조회
    fun getAllMembers(): List<MemberResponseDto> {
        val members = memberRepository.findAll()
        return members.map { MemberResponseDto.fromEntity(it) }
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    fun purgeExpiredDeletedMembers() {
        val cutoffDate = LocalDateTime.now().minusDays(30)
        val expiredMembers = memberRepository.findByDeletedTrueAndDeletedAtBefore(cutoffDate)
        memberRepository.deleteAll(expiredMembers) // 실제 DB에서 삭제
    }

    fun isSoftDeleted(memberId: Long): Boolean {
        val member = memberRepository.findById(memberId)
            .orElseThrow { EntityNotFoundException("회원을 찾을 수 없습니다.") }
        // deleted 필드가 있다고 가정
        return member.deleted
    }

    @Transactional
    @Throws(IOException::class)
    fun uploadProfileImage(memberId: Long, profileImage: MultipartFile): String? {

        val member = memberRepository.findById(memberId)
            .orElseThrow { EntityNotFoundException("회원을 찾을 수 없습니다.") }

        // 기존 프로필 이미지 삭제 (선택 사항)
        member.profileImage?.let {
            imageUtil.deleteImage(it)
        }

        // 새 이미지 저장
        val profileImageUrl = imageUtil.saveImage(profileImage)
        member.profileImage = profileImageUrl
        memberRepository.save(member)

        return profileImageUrl // 저장된 이미지 경로 반환
    }

    @Transactional
    @Throws(IOException::class)
    fun deleteProfileImage(memberId: Long) {

        val member = memberRepository.findById(memberId)
            .orElseThrow { EntityNotFoundException("회원을 찾을 수 없습니다.") }

        member.profileImage?.let {
            imageUtil.deleteImage(it)
            member.profileImage = null
            memberRepository.save(member)
        }
    }
}
