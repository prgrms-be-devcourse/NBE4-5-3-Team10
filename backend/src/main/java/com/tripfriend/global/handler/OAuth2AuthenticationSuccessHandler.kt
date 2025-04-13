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
        // 클라이언트의 Origin 헤더나 Referer 헤더를 확인하여 요청 출처 확인
        val origin = request.getHeader("Origin") ?: request.getHeader("Referer")
    
        // 클라이언트에서 전달받은 redirect_uri 파라미터 사용
        val redirectUri = request.getParameter("redirect_uri")
        println("클라이언트에서 전달받은 redirect_uri: $redirectUri")
    
        // 리다이렉트 URI가 있으면 사용, 없으면 요청 출처의 도메인을 사용
        val targetUrl = when {
            // redirect_uri 파라미터가 있으면 사용
            redirectUri != null -> redirectUri
        
            // Origin/Referer에서 도메인 추출 가능하면 사용
            origin != null -> {
                val domainPattern = "(https?://[^/]+)".toRegex()
                val domainMatch = domainPattern.find(origin)
                if (domainMatch != null) {
                    "${domainMatch.value}/member/login"
                } else {
                    "https://tripfriend.o-r.kr/member/login" // 기본값
                }
            }
        
        // 기본값
        else -> "https://tripfriend.o-r.kr/member/login"
    }
    
    println("사용할 redirect_uri: $targetUrl")
    return targetUrl
    }

    private fun addCookie(response: HttpServletResponse, request: HttpServletRequest, name: String, value: String, maxAge: Int) {
        val cookie = Cookie(name, value)
        cookie.path = "/" // 쿠키 경로 설정
        cookie.maxAge = maxAge // 만료 시간 설정
        cookie.isHttpOnly = true // 자바스크립트에서 접근 불가
    
        // 요청 URL을 확인하여 개발 환경(localhost)인지 판단
        val requestURL = request.requestURL.toString()
        val isLocalhost = requestURL.contains("localhost")
    
        if (!isLocalhost) {
            cookie.secure = true // HTTPS 환경에서만 사용 가능
            cookie.domain = "tripfriend.o-r.kr" // 도메인 설정
        }
    
        response.addCookie(cookie) // 쿠키를 응답에 추가
    }
}
