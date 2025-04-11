package com.tripfriend.global.security

import com.tripfriend.global.filter.DeletedMemberFilter
import com.tripfriend.global.filter.JwtAuthenticationFilter
import com.tripfriend.global.handler.OAuth2AuthenticationSuccessHandler
import com.tripfriend.global.util.JwtUtil
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig(
    private val jwtUtil: JwtUtil,
    private val customUserDetailsService: CustomUserDetailsService,
    private val customOauth2UserService: CustomOauth2UserService,
    private val oAuth2AuthenticationSuccessHandler: OAuth2AuthenticationSuccessHandler,
    private val deletedMemberFilter: DeletedMemberFilter,
    private val redisTemplate: RedisTemplate<String, String>
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            .csrf { csrf -> csrf.disable() }
            // H2 콘솔 접근을 허용하기 위해 frameOptions 비활성화
            .headers { headers -> headers.frameOptions { frame -> frame.disable() } }
            .sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // 항상 모든 HTTP 메소드에 대해 인증 없이 접근 가능한 경로
                    .requestMatchers(*getPublicEndpoints().toTypedArray()).permitAll()
                    // POST 요청에 대해 인증 없이 접근 허용
                    .requestMatchers(HttpMethod.POST, *getPublicPostEndpoints().toTypedArray()).permitAll()
                    // GET 요청에 대해서만 인증 없이 접근 허용하는 경로들
                    .requestMatchers(HttpMethod.GET, *getPublicGetEndpoints().toTypedArray()).permitAll()
                    // 나머지 모든 요청은 인증 필요
                    .anyRequest().authenticated()
            }
            // 인증 실패 시 401 응답 반환하도록 설정
            .exceptionHandling { exceptionHandling ->
                exceptionHandling
                    .authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(deletedMemberFilter, UsernamePasswordAuthenticationFilter::class.java)
            // oauth2 로그인 설정
            .oauth2Login { oauth2 ->
                oauth2
                    .userInfoEndpoint { userInfo ->
                        userInfo.userService(customOauth2UserService)
                    }
                    .successHandler(oAuth2AuthenticationSuccessHandler)
            }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        // 허용할 출처를 명시합니다.
        configuration.allowedOrigins = listOf(
            "https://tripfriend.o-r.kr", 
            "http://localhost:3000"
        )
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        // 모든 엔드포인트에 대해 CORS 설정 적용
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun jwtAuthenticationFilter(): JwtAuthenticationFilter {
        return JwtAuthenticationFilter(jwtUtil, customUserDetailsService, redisTemplate)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    private fun getPublicEndpoints(): List<String> {
        return listOf(
            // Swagger UI 관련 경로 허용
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",

            // h2-console 확인
            "/h2-console/**",

            // 항상 모든 HTTP 메소드에 대해 인증 없이 접근 가능한 경로
            "/notice",
            "/recruits/recent3",
            "/recruits/search",
            "/recruits/search2",
            "/recruits/search3",

            // OAuth2 관련 경로
            "/login",
            "/oauth2/authorization/**"
        )
    }

    private fun getPublicPostEndpoints(): List<String> {
        return listOf(
            // 회원 관련 POST 요청
            "/member/join",
            "/member/login",
            "/member/auth/email"
        )
    }

    private fun getPublicGetEndpoints(): List<String> {
        return listOf(
            // Place, Review, Comment, Recruit, Notice, QnA 등 GET 요청만 허용할 경로들
            "/place",
            "/place/{id}",
            "/place/search",
            "/place/cities",
            "/api/reviews/{reviewId}",
            "/api/reviews",
            "/api/reviews/popular",
            "/api/reviews/place/{placeId}",
            "/api/reviews/member/{memberId}",
            "/api/comments/{commentId}",
            "/api/comments/review/{reviewId}",
            "/api/comments/place/{placeId}",
            "/api/comments/comments/popular",
            "/api/comments/comments/member/{memberId}",
            "/recruits",
            "/recruits/{recruitId}",
            "/recruits/{recruitId}/applies",
            "/admin/notice/{id}",
            "/admin/event",
            "/qna",
            "/qna/{id}",
            "/qna/{questionId}/answers",
            "/member/auth/verify-email",
            "/images/**"
        )
    }
}
