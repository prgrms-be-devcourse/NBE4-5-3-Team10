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
            member.verified // 사용자 인증 여부 추가
        )
        
        // JWT 리프레시 토큰 생성
        val refreshToken = jwtUtil.generateRefreshToken(
            member.username,
            member.authority,
            member.verified
        )
        
        // 쿠키에 토큰 추가
        addCookie(response, request, "accessToken", accessToken, 30 * 60) // 30분
        addCookie(response, request, "refreshToken", refreshToken, 60 * 60 * 24 * 7) // 7일
        
        // 리디렉션 URI 가져오기
        var targetUrl = determineTargetUrl(request, response, authentication)
        
        // 리디렉션 URI에 토큰 추가
        targetUrl = if (targetUrl.contains("?")) {
            "$targetUrl&accessToken=$accessToken&refreshToken=$refreshToken"
        } else {
            "$targetUrl?accessToken=$accessToken&refreshToken=$refreshToken"
        }
        
        println("최종 리다이렉트 URL: $targetUrl")
        
        // 리디렉션 수행
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
    
    override fun determineTargetUrl(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ): String {
        // 클라이언트에서 전달받은 redirect_uri 파라미터를 최우선으로 사용
        val redirectUri = request.getParameter("redirect_uri")
        println("클라이언트에서 전달받은 redirect_uri: $redirectUri")
        
        if (!redirectUri.isNullOrBlank()) {
            // 보안을 위해 redirect_uri가 허용된 도메인인지 확인 (옵션)
            if (isValidRedirectUri(redirectUri)) {
                return redirectUri
            }
            println("유효하지 않은 redirect_uri: $redirectUri, 기본값 사용")
        }
        
        // 배포 환경을 기본으로 설정 (안전한 폴백 옵션)
        return "https://tripfriend.o-r.kr/member/login"
    }
    
    // 리다이렉트 URI의 유효성 검사 (필요에 따라 구현)
    private fun isValidRedirectUri(uri: String): Boolean {
        // 허용된 도메인 목록
        val allowedDomains = listOf(
            "tripfriend.o-r.kr",
            "www.tripfriend.o-r.kr",
            "localhost",
            "127.0.0.1"
        )
        
        // URI에 허용된 도메인이 포함되어 있는지 확인
        return allowedDomains.any { domain -> 
            uri.contains("://$domain") || uri.contains("://$domain:")
        }
    }
    
    private fun addCookie(
        response: HttpServletResponse,
        request: HttpServletRequest,
        name: String,
        value: String,
        maxAge: Int
    ) {
        val cookie = Cookie(name, value)
        cookie.path = "/" // 쿠키 경로 설정
        cookie.maxAge = maxAge // 만료 시간 설정
        cookie.isHttpOnly = true // 자바스크립트에서 접근 불가
        
        // 요청 URL을 확인하여 개발 환경(localhost)인지 판단
        val serverName = request.serverName
        
        // localhost가 아니면 secure 및 domain 설정
        if (!serverName.contains("localhost") && !serverName.contains("127.0.0.1")) {
            cookie.secure = true // HTTPS 환경에서만 사용 가능
            cookie.domain = "tripfriend.o-r.kr" // 도메인 설정
        }
        
        response.addCookie(cookie) // 쿠키를 응답에 추가
    }
}
