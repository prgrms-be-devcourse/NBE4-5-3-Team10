package com.tripfriend.global.filter

import com.tripfriend.global.util.JwtUtil
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class DeletedMemberFilter(
    private val jwtUtil: JwtUtil
) : OncePerRequestFilter() {

    // 허용할 엔드포인트 목록
    private val allowedEndpoints = listOf(
        "member/restore",
        "member/login",
        "member/logout"
    )

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestURI = request.requestURI

        // 허용된 엔드포인트인 경우 항상 통과
        if (allowedEndpoints.any { requestURI.endsWith(it) }) {
            filterChain.doFilter(request, response)
            return
        }

        // 토큰 확인
        var token = request.getHeader("Authorization")
        if (token == null || token.isEmpty()) {
            filterChain.doFilter(request, response)
            return
        }

        // Bearer 토큰 형식인 경우 "Bearer " 제거
        if (token.startsWith("Bearer ")) {
            token = token.substring(7)
        }

        try {
            // 소프트 딜리트된 사용자인지 확인
            if (jwtUtil.isDeletedAccount(token)) {
                response.status = HttpServletResponse.SC_FORBIDDEN
                response.contentType = "application/json"
                response.characterEncoding = "UTF-8"
                response.writer.write("{\"resultCode\":\"403\",\"msg\":\"계정이 비활성화되었습니다. 복구가 필요합니다.\"}")
                return
            }
        } catch (ex: Exception) {
            // 토큰 처리 중 오류 발생 시 다음 필터로 넘김
            filterChain.doFilter(request, response)
            return
        }

        filterChain.doFilter(request, response)
    }
}
