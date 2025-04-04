package com.tripfriend.domain.member.member.service;

import com.tripfriend.domain.member.member.dto.EmailVerificationRequestDto;
import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.repository.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    private final MemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${spring.mail.properties.auth-code-expiration-millis}")
    private long authCodeExpirationMillis;

    // Redis에 키 저장 시 접두어 (선택사항)
    private static final String EMAIL_AUTH_PREFIX = "EMAIL_AUTH:";

    public String createCode() {
        Random random = new Random();
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < 6; i++) { // 인증 코드 6자리
            int index = random.nextInt(2); // 0~1까지 랜덤, 랜덤값으로 switch문 실행

            switch (index) {
                case 0 -> key.append((char) (random.nextInt(26) + 65)); // 대문자
                case 1 -> key.append(random.nextInt(10)); // 숫자
            }
        }
        return key.toString();
    }

    public MimeMessage createMail(String mail, String authCode) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        message.setFrom(senderEmail);
        message.setRecipients(MimeMessage.RecipientType.TO, mail);
        message.setSubject("이메일 인증");
        String body = "";
        body += "<h3>요청하신 인증 번호입니다.</h3>";
        body += "<h1>" + authCode + "</h1>";
        body += "<h3>감사합니다.</h3>";
        message.setText(body, "UTF-8", "html");

        return message;
    }

    // 메일 발송
    public String sendSimpleMessage(String sendEmail) throws MessagingException {
        String authCode = createCode(); // 랜덤 인증번호 생성

        MimeMessage message = createMail(sendEmail, authCode); // 메일 생성
        try {
            javaMailSender.send(message); // 메일 발송
            return authCode;
        } catch (MailException e) {
            return null;
        }
    }

    @Transactional
    public boolean sendAuthCode(String email) throws MessagingException {
        String authCode = sendSimpleMessage(email); // 이메일 인증 코드 발송

        if (authCode != null) {
            // Redis에 인증 코드 저장 (만료 시간 설정)
            ValueOperations<String, String> values = redisTemplate.opsForValue();
            // 키 이름에 접두어 추가해서 저장
            String key = EMAIL_AUTH_PREFIX + email;

            // Redis에 저장하고 만료 시간 설정 (밀리초를 초 단위로 변환)
            values.set(key, authCode);
            redisTemplate.expire(key, 300, TimeUnit.SECONDS); // 5분

            return true;
        }
        return false;
    }

    public boolean validationAuthCode(EmailVerificationRequestDto emailVerificationRequestDto) {
        String email = emailVerificationRequestDto.getEmail();
        String authCode = emailVerificationRequestDto.getAuthCode();

        // Redis에서 인증 코드 조회
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        String key = EMAIL_AUTH_PREFIX + email;
        String storedAuthCode = values.get(key);

        // 인증 코드 검증
        if (storedAuthCode != null && storedAuthCode.equals(authCode)) {
            // 인증 성공 시 Member 엔티티의 verified 필드 업데이트
            Member member = memberRepository.findByEmail(email).orElse(null);

            if (member != null) {
                member.setVerified(true);
                memberRepository.save(member);
            }

            // 인증 성공 후 Redis에서 인증 코드 삭제
            redisTemplate.delete(key);

            return true;
        }
        return false;
    }
}
