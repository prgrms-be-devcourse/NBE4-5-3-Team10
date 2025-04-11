package com.tripfriend.domain.member.member.service

import com.tripfriend.domain.member.member.dto.EmailVerificationRequestDto
import com.tripfriend.domain.member.member.repository.MemberRepository
import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class MailService(
    private val javaMailSender: JavaMailSender,
    private val memberRepository: MemberRepository,
    private val redisTemplate: StringRedisTemplate
) {

    @Value("\${spring.mail.username}")
    private lateinit var senderEmail: String

    @Value("\${spring.mail.properties.auth-code-expiration-millis}")
    private var authCodeExpirationMillis: Long = 0

    // Redis에 키 저장 시 접두어 (선택사항)
    companion object {
        private const val EMAIL_AUTH_PREFIX = "EMAIL_AUTH:"
    }

    fun createCode(): String {
        val random = Random()
        val key = StringBuilder()

        for (i in 0 until 6) { // 인증 코드 6자리
            val index = random.nextInt(2) // 0~1까지 랜덤, 랜덤값으로 when 표현식 실행

            when (index) {
                0 -> key.append((random.nextInt(26) + 65).toChar()) // 대문자
                1 -> key.append(random.nextInt(10)) // 숫자
            }
        }
        return key.toString()
    }

    @Throws(MessagingException::class)
    fun createMail(mail: String, authCode: String): MimeMessage {
        val message = javaMailSender.createMimeMessage()

        message.setFrom(senderEmail)
        message.setRecipients(MimeMessage.RecipientType.TO, mail)
        message.setSubject("이메일 인증")
        var body = ""
        body += "<h3>요청하신 인증 번호입니다.</h3>"
        body += "<h1>$authCode</h1>"
        body += "<h3>감사합니다.</h3>"
        message.setText(body, "UTF-8", "html")

        return message
    }

    // 메일 발송
    @Throws(MessagingException::class)
    fun sendSimpleMessage(sendEmail: String): String? {
        val authCode = createCode() // 랜덤 인증번호 생성

        val message = createMail(sendEmail, authCode) // 메일 생성
        return try {
            javaMailSender.send(message) // 메일 발송
            authCode
        } catch (e: MailException) {
            null
        }
    }

    @Transactional
    @Throws(MessagingException::class)
    fun sendAuthCode(email: String): Boolean {
        val authCode = sendSimpleMessage(email) // 이메일 인증 코드 발송

        if (authCode != null) {
            // Redis에 인증 코드 저장 (만료 시간 설정)
            val values = redisTemplate.opsForValue()
            // 키 이름에 접두어 추가해서 저장
            val key = EMAIL_AUTH_PREFIX + email

            // Redis에 저장하고 만료 시간 설정 (밀리초를 초 단위로 변환)
            values.set(key, authCode)
            redisTemplate.expire(key, 300, TimeUnit.SECONDS) // 5분

            return true
        }
        return false
    }

    fun validationAuthCode(emailVerificationRequestDto: EmailVerificationRequestDto): Boolean {
        val email = emailVerificationRequestDto.email
        val authCode = emailVerificationRequestDto.authCode

        // Redis에서 인증 코드 조회
        val values = redisTemplate.opsForValue()
        val key = EMAIL_AUTH_PREFIX + email
        val storedAuthCode = values.get(key)

        // 인증 코드 검증
        if (storedAuthCode != null && storedAuthCode == authCode) {
            // 인증 성공 시 Member 엔티티의 verified 필드 업데이트
            val member = memberRepository.findByEmail(email).orElse(null)

            if (member != null) {
                member.verified = true
                memberRepository.save(member)
            }

            // 인증 성공 후 Redis에서 인증 코드 삭제
            redisTemplate.delete(key)

            return true
        }
        return false
    }
}
