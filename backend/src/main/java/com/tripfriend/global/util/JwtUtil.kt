package com.tripfriend.global.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Component
class JwtUtil(
    private val redisTemplate: RedisTemplate<String, String>
) {

    @Value("\${custom.jwt.secret-key}")
    private lateinit var secretKey: String

    @Value("\${custom.jwt.access-token-expiration}")
    private var accessTokenExpiration: Long = 0

    @Value("\${custom.jwt.refresh-token-expiration}")
    private var refreshTokenExpiration: Long = 0

    companion object {
        private const val REDIS_ACCESS_TOKEN_PREFIX = "access:"
        private const val REDIS_REFRESH_TOKEN_PREFIX = "refresh:"
        private const val REDIS_BLACKLIST_PREFIX = "blacklist:"
    }

    // 액세스 토큰 생성 - Redis에 저장하고 클라이언트에 반환
    fun generateAccessToken(username: String, authority: String, verified: Boolean): String {
        val token = generateToken(username, authority, verified, accessTokenExpiration)

        // Redis에 액세스 토큰 저장
        redisTemplate.opsForValue().set(
            REDIS_ACCESS_TOKEN_PREFIX + username,
            token,
            accessTokenExpiration,
            TimeUnit.MILLISECONDS
        )

        return token
    }

    // 삭제된 계정용 액세스 토큰 생성
    fun generateAccessToken(username: String, authority: String, verified: Boolean, deleted: Boolean): String {
        val token = generateToken(username, authority, verified, deleted, accessTokenExpiration)

        // Redis에 액세스 토큰 저장 (짧은 유효기간)
        redisTemplate.opsForValue().set(
            REDIS_ACCESS_TOKEN_PREFIX + username,
            token,
            10 * 60 * 1000, // 10분
            TimeUnit.MILLISECONDS
        )

        return token
    }

    // 리프레시 토큰 생성 - Redis에만 저장
    fun generateRefreshToken(username: String, authority: String, verified: Boolean): String {
        val refreshToken = generateToken(username, authority, verified, refreshTokenExpiration)

        // Redis에 리프레시 토큰 저장
        redisTemplate.opsForValue().set(
            REDIS_REFRESH_TOKEN_PREFIX + username,
            refreshToken,
            refreshTokenExpiration,
            TimeUnit.MILLISECONDS
        )

        return refreshToken
    }

    // 삭제된 계정용 리프레시 토큰 생성
    fun generateRefreshToken(username: String, authority: String, verified: Boolean, deleted: Boolean): String {
        val refreshToken = generateToken(username, authority, verified, deleted, refreshTokenExpiration)

        // 복구 가능한 삭제된 계정용 리프레시 토큰은 짧은 시간만 유효하게 설정 (10분)
        redisTemplate.opsForValue().set(
            REDIS_REFRESH_TOKEN_PREFIX + username,
            refreshToken,
            10 * 60 * 1000, // 10분
            TimeUnit.MILLISECONDS
        )

        return refreshToken
    }

    // 공통 토큰 생성 메서드
    private fun generateToken(username: String, authority: String, verified: Boolean, expirationTime: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationTime)

        val claims = HashMap<String, Any>()
        claims["authority"] = authority
        claims["verified"] = verified

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .addClaims(claims)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact()
    }

    // 소프트딜리트 정보가 포함된 토큰 생성 메서드
    private fun generateToken(username: String, authority: String, verified: Boolean, deleted: Boolean, expirationTime: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationTime)

        val claims = HashMap<String, Any>()
        claims["authority"] = authority
        claims["verified"] = verified
        claims["deleted"] = deleted // 삭제 여부 추가

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .addClaims(claims)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact()
    }

    // 토큰 블랙리스트에 추가 (로그아웃 시 사용)
    fun addToBlacklist(token: String) {
        // 토큰의 남은 유효 시간 계산
        val expiration = getClaims(token).expiration.time
        val now = System.currentTimeMillis()
        val ttl = expiration - now

        if (ttl > 0) {
            // 블랙리스트에 토큰 추가 (만료 시간까지만 저장)
            redisTemplate.opsForValue().set(
                REDIS_BLACKLIST_PREFIX + token,
                "logout",
                ttl,
                TimeUnit.MILLISECONDS
            )

            // 사용자의 액세스 토큰도 Redis에서 삭제
            val username = extractUsername(token)
            redisTemplate.delete(REDIS_ACCESS_TOKEN_PREFIX + username)
        }
    }

    // Redis에서 리프레시 토큰 검증
    fun validateRefreshTokenInRedis(username: String, refreshToken: String): Boolean {
        val storedToken = redisTemplate.opsForValue().get(REDIS_REFRESH_TOKEN_PREFIX + username)
        return refreshToken == storedToken
    }

    // Redis에서 액세스 토큰 검증
    fun validateAccessTokenInRedis(username: String, accessToken: String): Boolean {
        val storedToken = redisTemplate.opsForValue().get(REDIS_ACCESS_TOKEN_PREFIX + username)
        return accessToken == storedToken
    }

    // 토큰이 블랙리스트에 있는지 확인
    fun isTokenBlacklisted(token: String): Boolean {
        return redisTemplate.hasKey(REDIS_BLACKLIST_PREFIX + token) == true
    }

    // 기존 토큰 관련 메서드들은 그대로 유지
    fun extractUsername(token: String): String {
        return getClaims(token).subject
    }

    fun extractAuthority(token: String): String {
        return getClaims(token).get("authority", String::class.java)
    }

    fun extractVerified(token: String): Boolean {
        val verifiedClaim = getClaims(token)["verified"]
        return verifiedClaim is Boolean && verifiedClaim == true
    }

    fun isTokenExpired(token: String): Boolean {
        return getClaims(token).expiration.before(Date())
    }

    // 토큰 유효성 검증 (블랙리스트 확인 및 Redis 검증 추가)
    fun validateToken(token: String, username: String): Boolean {
        return (username == extractUsername(token) &&
                !isTokenExpired(token) &&
                !isTokenBlacklisted(token) &&
                validateAccessTokenInRedis(username, token))
    }

    fun getClaims(token: String): Claims {
        return Jwts.parser()
            .setSigningKey(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun getSigningKey(): SecretKey {
        return SecretKeySpec(secretKey.toByteArray(), SignatureAlgorithm.HS512.jcaName)
    }

    fun isDeletedAccount(token: String): Boolean {
        val deletedClaim = getClaims(token)["deleted"]
        return deletedClaim is Boolean && deletedClaim == true
    }

    fun getRefreshTokenExpiration(): Long {
        return refreshTokenExpiration
    }

    // 액세스 토큰 키 값 조회를 위한 메서드
    fun getStoredAccessToken(username: String): String? {
        return redisTemplate.opsForValue().get(REDIS_ACCESS_TOKEN_PREFIX + username)
    }

    // 리프레시 토큰 키 값 조회를 위한 메서드
    fun getStoredRefreshToken(username: String): String? {
        return redisTemplate.opsForValue().get(REDIS_REFRESH_TOKEN_PREFIX + username)
    }
}
