package com.tripfriend.global.handler

import com.tripfriend.global.security.PrincipalDetails
import com.tripfriend.global.util.JwtUtil
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtUtil: JwtUtil
) : SimpleUrlAuthenticationSuccessHandler() {

    @Throws(IOException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val principalDetails = authentication.principal as PrincipalDetails
        val member = principalDetails.getMember()

        // JWT 토큰 생성 (JwtUtil 사용)
        val accessToken = jwtUtil.generateAccessToken(
            member.username,
            member.authority,
            member.verified  // 사용자 인증 여부 추가
        )

        // JWT 리프레시 토큰 생성
        val refreshToken = jwtUtil.generateRefreshToken(
            member.username,
            member.authority,
            member.verified
        )

        addCookie(response, "accessToken", accessToken, 30 * 60) // 30분
        addCookie(response, "refreshToken", refreshToken, 60 * 60 * 24 * 7) // 7일

        // 리디렉션 URI 가져오기 (클라이언트에서 전달한 redirect_uri 파라미터 값)
        var targetUrl = determineTargetUrl(request, response, authentication)

        // 리디렉션 URI에 토큰 추가
        targetUrl = if (targetUrl.contains("?")) {
            "$targetUrl&accessToken=$accessToken&refreshToken=$refreshToken"
        } else {
            "$targetUrl?accessToken=$accessToken&refreshToken=$refreshToken"
        }

        // 리디렉션 수행
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    override fun determineTargetUrl(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ): String {
        // 세션에 저장된 원래 리디렉션 URI 가져오기, 없으면 기본값 사용
        val defaultRedirectUri = "http://localhost:3000/member/login"
        val redirectUri = request.getParameter("redirect_uri")
        return redirectUri ?: defaultRedirectUri
    }

    // 쿠키 생성 메서드 (만료 시간 설정)
    private fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Int) {
        val cookie = Cookie(name, value)
        cookie.path = "/"
        cookie.maxAge = maxAge // 만료 시간 설정
        cookie.isHttpOnly = true // 자바스크립트에서 접근 불가
        cookie.secure = true // HTTPS 환경에서만 사용 가능
        response.addCookie(cookie)
    }
}
