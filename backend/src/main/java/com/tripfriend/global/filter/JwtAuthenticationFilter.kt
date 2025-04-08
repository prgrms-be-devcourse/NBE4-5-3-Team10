package com.tripfriend.global.filter

import com.tripfriend.global.security.CustomUserDetailsService
import com.tripfriend.global.util.JwtUtil
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val userDetailsService: CustomUserDetailsService,
    private val redisTemplate: RedisTemplate<String, String>
) : OncePerRequestFilter() {

    companion object {
        private const val REDIS_BLACKLIST_PREFIX = "blacklist:"
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val headerToken = extractTokenFromHeader(request)
        val cookieToken = extractTokenFromCookie(request)

        val token = headerToken ?: cookieToken

        if (token != null) {
            try {
                if (isTokenBlacklisted(token)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그아웃된 토큰입니다.")
                    return
                }

                val claims: Claims = jwtUtil.getClaims(token)
                val username = claims.subject
                val authority = claims["authority"] as? String
                val isVerified = claims["verified"] as? Boolean

                if (!validateAccessTokenInRedis(username, token)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.")
                    return
                }

                if (username != null && isVerified == true) {
                    val userDetails: UserDetails = userDetailsService.loadUserByUsername(username)
                    val authorities: List<GrantedAuthority> = listOf(SimpleGrantedAuthority("ROLE_$authority"))

                    val authentication = UsernamePasswordAuthenticationToken(userDetails, null, authorities)
                    SecurityContextHolder.getContext().authentication = authentication
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "이메일 인증이 완료되지 않았습니다.")
                    return
                }
            } catch (e: ExpiredJwtException) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 만료되었습니다.")
                return
            } catch (e: JwtException) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.")
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun extractTokenFromHeader(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }

    private fun extractTokenFromCookie(request: HttpServletRequest): String? {
        val cookies: Array<Cookie>? = request.cookies
        cookies?.forEach { cookie ->
            if (cookie.name == "accessToken") {
                return cookie.value
            }
        }
        return null
    }

    private fun isTokenBlacklisted(token: String): Boolean {
        return redisTemplate.hasKey(REDIS_BLACKLIST_PREFIX + token) == true
    }

    private fun validateAccessTokenInRedis(username: String, token: String): Boolean {
        val storedToken = redisTemplate.opsForValue().get("access:$username")
        return token == storedToken
    }
}
