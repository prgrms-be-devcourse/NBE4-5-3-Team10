package com.tripfriend.domain.member.member.service

import com.tripfriend.domain.member.member.dto.AuthResponseDto
import com.tripfriend.domain.member.member.dto.LoginRequestDto
import com.tripfriend.domain.member.member.dto.TokenInfoDto
import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.member.member.repository.MemberRepository
import com.tripfriend.global.util.JwtUtil
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jwtUtil: JwtUtil,
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val redisTemplate: RedisTemplate<String, String>
) {

    // 로그인 처리
    fun login(loginRequestDto: LoginRequestDto, response: HttpServletResponse): AuthResponseDto {
        // 회원 인증 처리
        val member = memberRepository.findByUsername(loginRequestDto.username)
            .orElseThrow { UsernameNotFoundException("존재하지 않는 회원입니다.") }

        if (!passwordEncoder.matches(loginRequestDto.password, member.password)) {
            throw RuntimeException("비밀번호를 확인하세요.")
        }

        // 계정이 삭제된 상태인 경우
        if (member.deleted) {
            // 복구 가능한 경우
            if (member.canBeRestored()) {
                // 복구 가능한 경우에만 특별한 토큰 발급
                val accessToken = jwtUtil.generateAccessToken(member.username, member.authority, member.verified, true)
                val refreshToken = jwtUtil.generateRefreshToken(member.username, member.authority, member.verified, true)

                // 액세스 토큰과 리프레시 토큰을 쿠키에 저장
                addCookie(response, "accessToken", accessToken, 10 * 60) // 10분
                addCookie(response, "refreshToken", refreshToken, 60 * 60 * 24) // 1일

                // 토큰 정보와 삭제 여부, 권한 정보를 응답에 포함
                return AuthResponseDto(accessToken, refreshToken, true, member.authority)
            } else {
                throw RuntimeException("영구 삭제된 계정입니다. 새로운 계정으로 가입해주세요.")
            }
        }

        // 토큰 생성 (Redis에 저장됨)
        val accessToken = jwtUtil.generateAccessToken(member.username, member.authority, member.verified)
        val refreshToken = jwtUtil.generateRefreshToken(member.username, member.authority, member.verified)

        // 액세스 토큰과 리프레시 토큰을 쿠키에 저장
        addCookie(response, "accessToken", accessToken, 30 * 60) // 30분
        addCookie(response, "refreshToken", refreshToken, 60 * 60 * 24 * 7) // 7일

        // 토큰 정보와 삭제 여부, 권한 정보를 응답에 포함
        return AuthResponseDto(accessToken, refreshToken, false, member.authority)
    }

    // 로그아웃 처리 - Redis 블랙리스트 활용
    fun logout(request: HttpServletRequest, response: HttpServletResponse) {
        // 쿠키에서 액세스 토큰 추출
        val cookies = request.cookies
        cookies?.forEach { cookie ->
            if ("accessToken" == cookie.name) {
                // 액세스 토큰을 블랙리스트에 추가
                val accessToken = cookie.value
                if (!accessToken.isNullOrEmpty()) {
                    val username = jwtUtil.extractUsername(accessToken)

                    // 액세스 토큰 블랙리스트에 추가
                    jwtUtil.addToBlacklist(accessToken)

                    // Redis에서 리프레시 토큰 삭제
                    redisTemplate.delete("refresh:$username")
                }
            }
        }

        // 쿠키에서 액세스 토큰 삭제
        addCookie(response, "accessToken", null, 0)
    }

    // 리프레시 토큰으로 새로운 액세스 토큰 발급 - Redis 검증 추가
    fun refreshToken(accessToken: String, response: HttpServletResponse): AuthResponseDto {
        // 만료된 액세스 토큰에서 사용자명 추출
        val username = jwtUtil.extractUsername(accessToken)

        // Redis에서 해당 사용자의 리프레시 토큰 확인
        val storedRefreshToken = jwtUtil.getStoredRefreshToken(username)
            ?: throw RuntimeException("저장된 리프레시 토큰이 없습니다")

        // 리프레시 토큰 검증
        if (jwtUtil.isTokenExpired(storedRefreshToken)) {
            throw RuntimeException("리프레시 토큰이 만료되었습니다")
        }

        // 액세스 토큰에서 필요한 정보 추출
        val authority = jwtUtil.extractAuthority(accessToken)
        val isVerified = jwtUtil.extractVerified(accessToken)
        val isDeleted = jwtUtil.isDeletedAccount(accessToken)

        // 새로운 액세스 토큰 생성
        val newAccessToken: String
        if (isDeleted) {
            newAccessToken = jwtUtil.generateAccessToken(username, authority, isVerified, true)
            addCookie(response, "accessToken", newAccessToken, 10 * 60) // 10분
        } else {
            newAccessToken = jwtUtil.generateAccessToken(username, authority, isVerified)
            addCookie(response, "accessToken", newAccessToken, 30 * 60) // 30분
        }

        var newRefreshToken = storedRefreshToken

        // 리프레시 토큰 재발급 필요성 확인
        if (isRefreshTokenNeedsRenewal(storedRefreshToken)) {
            if (isDeleted) {
                newRefreshToken = jwtUtil.generateRefreshToken(username, authority, isVerified, true)
                addCookie(response, "refreshToken", newRefreshToken, 60 * 60 * 24) // 1일
            } else {
                newRefreshToken = jwtUtil.generateRefreshToken(username, authority, isVerified)
                addCookie(response, "refreshToken", newRefreshToken, 60 * 60 * 24 * 7) // 7일
            }
        }

        // 토큰 정보와 권한 정보도 응답에 포함
        return AuthResponseDto(newAccessToken, newRefreshToken, isDeleted, authority)
    }

    // 리프레시 토큰 갱신 필요 여부 확인
    private fun isRefreshTokenNeedsRenewal(refreshToken: String): Boolean {
        return try {
            val claims = jwtUtil.getClaims(refreshToken)
            val expiration = claims.expiration

            // 만료까지 남은 시간 계산 (밀리초)
            val timeToExpire = expiration.time - System.currentTimeMillis()

            // 만료 기간의 30% 이하로 남았으면 갱신
            timeToExpire < (jwtUtil.getRefreshTokenExpiration() * 0.3)
        } catch (e: Exception) {
            true
        }
    }

    // 로그인된 사용자의 정보를 반환하는 메서드
    fun getLoggedInMember(token: String): Member {
        // 토큰에서 "Bearer "를 제거
        val extractedToken = token.replace("Bearer ", "")

        // 토큰이 블랙리스트에 있는지 확인
        if (jwtUtil.isTokenBlacklisted(extractedToken)) {
            throw RuntimeException("로그아웃된 토큰입니다.")
        }

        // Redis에 저장된 토큰과 일치하는지 확인
        val username = jwtUtil.extractUsername(extractedToken)
        if (!jwtUtil.validateAccessTokenInRedis(username, extractedToken)) {
            throw RuntimeException("유효하지 않은 토큰입니다.")
        }

        // 토큰에서 사용자 정보 추출
        val tokenInfo = extractTokenInfo(extractedToken)
        return memberRepository.findByUsername(tokenInfo.username)
            .orElseThrow { UsernameNotFoundException("User not found") }
    }

    // 토큰에서 사용자 정보를 추출하는 메서드
    fun extractTokenInfo(token: String): TokenInfoDto {
        if (jwtUtil.isTokenExpired(token)) {
            throw RuntimeException("만료된 토큰입니다.")
        }

        val username = jwtUtil.extractUsername(token)
        val authority = jwtUtil.extractAuthority(token)
        val isVerified = jwtUtil.extractVerified(token)

        return TokenInfoDto(username, authority, isVerified)
    }

    // 쿠키 생성 메서드 (만료 시간 설정)
    private fun addCookie(response: HttpServletResponse, name: String, value: String?, maxAge: Int) {
        val cookie = Cookie(name, value)
        cookie.path = "/"
        cookie.maxAge = maxAge // 만료 시간 설정
        cookie.isHttpOnly = true // 자바스크립트에서 접근 불가
        cookie.secure = true // HTTPS 환경에서만 사용 가능
        response.addCookie(cookie)
    }
}
