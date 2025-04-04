package com.tripfriend.global.filter;

import com.tripfriend.global.security.CustomUserDetailsService;
import com.tripfriend.global.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REDIS_BLACKLIST_PREFIX = "blacklist:";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 헤더에서 토큰 추출
        String headerToken = extractTokenFromHeader(request);

        // 쿠키에서 토큰 추출
        String cookieToken = extractTokenFromCookie(request);

        // 둘 중 하나라도 있으면 인증 처리
        String token = headerToken != null ? headerToken : cookieToken;

        if (token != null) {
            try {
                // 블랙리스트 확인
                if (isTokenBlacklisted(token)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그아웃된 토큰입니다.");
                    return;
                }

                Claims claims = jwtUtil.getClaims(token);
                String username = claims.getSubject();
                String authority = claims.get("authority", String.class);
                Boolean isVerified = claims.get("verified", Boolean.class);

                // Redis에 저장된 액세스 토큰과 일치하는지 확인
                if (!validateAccessTokenInRedis(username, token)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
                    return;
                }

                if (username != null && Boolean.TRUE.equals(isVerified)) {
                    // UserDetails 객체 생성 (DB에서 사용자 정보 조회)
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + authority));

                    // UserDetails 기반으로 Authentication 객체 생성
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                    // SecurityContextHolder에 인증 정보 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "이메일 인증이 완료되지 않았습니다.");
                    return;
                }
            } catch (ExpiredJwtException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 만료되었습니다.");
                return;
            } catch (JwtException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    // 헤더에서 토큰 추출
    private String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " 부분을 제거
        }
        return null;
    }

    // 쿠키에서 토큰 추출
    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // Redis에서 토큰 블랙리스트 확인
    private boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(REDIS_BLACKLIST_PREFIX + token));
    }

    // Redis에 저장된 액세스 토큰과 일치하는지 확인
    private boolean validateAccessTokenInRedis(String username, String token) {
        String storedToken = redisTemplate.opsForValue().get("access:" + username);
        return token.equals(storedToken);
    }
}
