package com.tripfriend.domain.member.member.controller

import com.tripfriend.domain.member.member.dto.*
import com.tripfriend.domain.member.member.service.AuthService
import com.tripfriend.domain.member.member.service.MailService
import com.tripfriend.domain.member.member.service.MemberService
import com.tripfriend.global.annotation.CheckPermission
import com.tripfriend.global.dto.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.mail.MessagingException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

@Tag(name = "Member API", description = "회원관련 기능을 제공합니다.")
@RestController
@RequestMapping("/member")
class MemberController(
    private val memberService: MemberService,
    private val authService: AuthService,
    private val mailService: MailService
) {

    // 회원정보 조회
    @Operation(summary = "회원정보 조회")
    @GetMapping("/me")
    fun getCurrentUser(@RequestHeader("Authorization") token: String): ResponseEntity<Map<String, Any>> {
        val member = authService.getLoggedInMember(token)

        val response = HashMap<String, Any>()
        response["id"] = member.id!!  // null 안전 연산자 추가
        response["username"] = member.username

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "회원가입")
    @PostMapping("/join")
    @Throws(MessagingException::class)
    fun join(@Valid @RequestBody joinRequestDto: JoinRequestDto): RsData<MemberResponseDto> {
        val savedMember = memberService.join(joinRequestDto)
        return RsData("201-1", "회원가입이 완료되었습니다.", savedMember)
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    fun login(@RequestBody loginRequestDto: LoginRequestDto, response: HttpServletResponse): RsData<AuthResponseDto> {
        val authResponse = authService.login(loginRequestDto, response)
        return RsData("200-1", "로그인 성공", authResponse)
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    fun logout(request: HttpServletRequest, response: HttpServletResponse): RsData<Unit> {  // Void -> Unit
        authService.logout(request, response)
        return RsData("200-1", "로그아웃이 완료되었습니다.", Unit)  // null -> Unit
    }

    @Operation(summary = "액세스 토큰 재발급")
    @PostMapping("/refresh")
    fun refresh(@CookieValue(name = "accessToken", required = false) accessToken: String?, response: HttpServletResponse): RsData<AuthResponseDto?> {  // nullable로 변경
        return try {
            if (accessToken == null) {
                return RsData("401-2", "액세스 토큰이 없습니다.", null)
            }

            val authResponseDto = authService.refreshToken(accessToken, response)
            RsData("200-1", "토큰이 재발급되었습니다.", authResponseDto)
        } catch (e: Exception) {
            RsData("401-1", "토큰 재발급에 실패했습니다: ${e.message}", null)
        }
    }

    @Operation(summary = "회원정보 수정")
    @PutMapping("/update")
    fun updateMember(
        @RequestHeader(value = "Authorization", required = false) token: String,
        @RequestBody memberUpdateRequestDto: MemberUpdateRequestDto
    ): RsData<MemberResponseDto> {
        val loggedInMember = authService.getLoggedInMember(token)

        val response = memberService.updateMember(loggedInMember.id!!, memberUpdateRequestDto)  // null 안전 연산자 추가
        return RsData("200-1", "회원 정보가 수정되었습니다.", response)
    }

    @Operation(summary = "회원 삭제")
    @DeleteMapping("/delete")
    fun deleteMember(
        @RequestHeader(value = "Authorization", required = false) token: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): RsData<Unit> {  // Void -> Unit
        val loggedInMember = authService.getLoggedInMember(token)

        memberService.deleteMember(loggedInMember.id!!, request, response)  // null 안전 연산자 추가
        return RsData("204-1", "회원이 삭제되었습니다.", Unit)  // null -> Unit
    }

    @Operation(summary = "회원 복구")
    @PostMapping("/restore")
    fun restoreMember(@RequestHeader(value = "Authorization", required = false) token: String): RsData<Unit> {  // Void -> Unit
        val loggedInMember = authService.getLoggedInMember(token)
        memberService.restoreMember(loggedInMember.id!!)  // null 안전 연산자 추가

        return RsData("200-1", "계정이 성공적으로 복구되었습니다.", Unit)  // null -> Unit
    }

    @Operation(summary = "이메일 인증 코드 전송")
    @GetMapping("/auth/verify-email")
    @Throws(MessagingException::class)
    fun requestAuthCode(email: String): RsData<Unit> {  // Void -> Unit
        val isSend = mailService.sendAuthCode(email)
        return if (isSend)
            RsData("200-1", "인증 코드가 전송되었습니다.", Unit)  // null -> Unit
        else
            RsData("500-1", "인증 코드 전송이 실패하였습니다.", Unit)  // null -> Unit
    }

    @Operation(summary = "이메일 인증")
    @PostMapping("/auth/email")
    fun validateAuthCode(@RequestBody @Valid emailVerificationRequestDto: EmailVerificationRequestDto): RsData<Unit> {  // Void -> Unit
        val isSuccess = mailService.validationAuthCode(emailVerificationRequestDto)
        return if (isSuccess)
            RsData("200-1", "이메일 인증에 성공하였습니다.", Unit)  // null -> Unit
        else
            RsData("400-1", "이메일 인증에 실패하였습니다.", Unit)  // null -> Unit
    }

    @Operation(summary = "마이페이지")
    @GetMapping("/mypage")
    fun getMyPage(@RequestHeader(value = "Authorization", required = false) token: String): RsData<MemberResponseDto> {
        val loggedInMember = authService.getLoggedInMember(token)

        val response = memberService.getMyPage(loggedInMember.id!!, loggedInMember.username)  // null 안전 연산자 추가
        return RsData("200-1", "마이페이지 정보 조회 성공", response)
    }

    // 관리자 회원 조회
    @Operation(summary = "전체 회원 목록 조회 (관리자 전용)")
    @GetMapping("/all")
    @CheckPermission("ADMIN") // 관리자
    fun getAllMembers(): ResponseEntity<List<MemberResponseDto>> {
        val members = memberService.getAllMembers()
        return ResponseEntity.ok(members)
    }

    @Operation(summary = "프로필 이미지 등록")
    @PostMapping(value = ["/profile-image/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Throws(IOException::class)
    fun uploadProfileImage(
        @RequestHeader(value = "Authorization", required = false) token: String,
        @RequestPart("image") imageFile: MultipartFile
    ): RsData<String> {
        val loggedInMember = authService.getLoggedInMember(token)
        val imageUrl = memberService.uploadProfileImage(loggedInMember.id!!, imageFile)  // null 안전 연산자 추가

        return RsData("200-1", "이미지 업로드 성공", imageUrl!!)
    }

    @Operation(summary = "프로필 이미지 삭제")
    @DeleteMapping("/profile-image/delete")
    @Throws(IOException::class)
    fun deleteProfileImage(@RequestHeader(value = "Authorization", required = false) token: String): RsData<String?> {
        val loggedInMember = authService.getLoggedInMember(token)

        if (loggedInMember.profileImage == null) {
            return RsData("400-1", "삭제할 이미지가 없습니다.", null)
        }

        memberService.deleteProfileImage(loggedInMember.id!!)  // null 안전 연산자 추가

        return RsData("200-1", "이미지 삭제 성공", null)
    }
}