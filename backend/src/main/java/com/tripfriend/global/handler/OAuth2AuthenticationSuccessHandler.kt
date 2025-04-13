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
        // 클라이언트에서 전달받은 redirect_uri 파라미터 사용 - 이 부분이 중요
        val redirectUri = request.getParameter("redirect_uri")
        println("클라이언트에서 전달받은 redirect_uri: $redirectUri")
        
        // redirect_uri 값이 있고 올바른 형식이면 그대로 사용
        if (!redirectUri.isNullOrBlank() && 
            (redirectUri.startsWith("http://") || redirectUri.startsWith("https://"))) {
            println("클라이언트 지정 redirect_uri 사용: $redirectUri")
            return redirectUri
        }
        
        // 클라이언트의 Origin 헤더나 Referer 헤더를 확인하여 요청 출처 확인
        val origin = request.getHeader("Origin") ?: request.getHeader("Referer")
        
        // 기본 타겟 URL 결정
        val targetUrl = when {
            // Origin/Referer에서 도메인 추출 가능하면 사용
            origin != null -> {
                val domainPattern = "(https?://[^/]+)".toRegex()
                val domainMatch = domainPattern.find(origin)
                if (domainMatch != null) {
                    "${domainMatch.value}/member/login"
                } else {
                    getFallbackUrl(request)
                }
            }
            // 요청 URL에서 도메인 추출
            else -> getFallbackUrl(request)
        }
        
        println("최종 결정된 redirect_uri: $targetUrl")
        return targetUrl
    }
    
    private fun getFallbackUrl(request: HttpServletRequest): String {
        // 현재 요청의 호스트 정보로부터 프론트엔드 URL 추정
        val serverName = request.serverName
        val requestURL = request.requestURL.toString()
        
        return when {
            // 로컬 개발 환경
            serverName.contains("localhost") || serverName.contains("127.0.0.1") -> {
                // 백엔드가 8080 포트를 사용하면 프론트엔드는 보통 3000 포트 사용
                "http://localhost:3000/member/login"
            }
            // 배포 환경
            serverName.contains("tripfriend.o-r.kr") -> {
                "https://tripfriend.o-r.kr/member/login"
            }
            // 기타 환경
            else -> {
                val domainPattern = "(https?://[^/]+)".toRegex()
                val domainMatch = domainPattern.find(requestURL)
                if (domainMatch != null) {
                    "${domainMatch.value}/member/login"
                } else {
                    "https://tripfriend.o-r.kr/member/login" // 최종 기본값
                }
            }
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
        val requestURL = request.requestURL.toString()
        val serverName = request.serverName
        
        // localhost가 아니고 실제 도메인인 경우에만 secure와 domain 설정
        if (!serverName.contains("localhost") && !serverName.contains("127.0.0.1")) {
            cookie.secure = true // HTTPS 환경에서만 사용 가능
            
            // 서버 이름에서 도메인 추출
            if (serverName.contains("tripfriend.o-r.kr")) {
                cookie.domain = "tripfriend.o-r.kr" // 도메인 설정
            }
        }
        
        response.addCookie(cookie) // 쿠키를 응답에 추가
    }
}
