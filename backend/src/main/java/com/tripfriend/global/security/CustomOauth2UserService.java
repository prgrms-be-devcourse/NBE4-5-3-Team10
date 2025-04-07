package com.tripfriend.global.security;

import com.tripfriend.domain.member.member.entity.AgeRange;
import com.tripfriend.domain.member.member.entity.Gender;
import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.entity.TravelStyle;
import com.tripfriend.domain.member.member.repository.MemberRepository;
import com.tripfriend.global.oauth.GoogleUserInfo;
import com.tripfriend.global.oauth.KakaoUserInfo;
import com.tripfriend.global.oauth.NaverUserInfo;
import com.tripfriend.global.oauth.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private static final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        OAuth2UserInfo oAuth2UserInfo = null;

        // 액세스 토큰 얻기
        String accessToken = userRequest.getAccessToken().getTokenValue();
        System.out.println("Access Token: " + accessToken);

        if (userRequest.getClientRegistration().getRegistrationId().equals("google")) {
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
            oAuth2UserInfo = new NaverUserInfo((Map)oAuth2User.getAttributes().get("response"));
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("kakao")) {
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        } else {
            log.info("error");
        }

        String randomPassword = generateRandomPassword(15);  // 15자리 랜덤 비밀번호 생성

        String provider = oAuth2UserInfo.getProvider();
        String providerId = oAuth2UserInfo.getProviderId();
        String username = provider + "_" + providerId;
        String password = bCryptPasswordEncoder.encode(randomPassword);
        String email = oAuth2UserInfo.getEmail();
        double rating = 0.0;
        String authority = "USER";
        boolean verified = true;

        // 사용자 조회
        Optional<Member> memberEntityOptional = memberRepository.findByUsername(username);

        if (memberEntityOptional.isEmpty()) {
            System.out.println("OAuth 로그인이 최초입니다.");

            // 새로운 Member 객체 생성 (기본 정보만 저장)
            Member memberEntity = new Member();

            memberEntity.setUsername(username);
            memberEntity.setEmail(email);
            memberEntity.setPassword(password);
            memberEntity.setNickname(username);
            memberEntity.setGender(Gender.UNKNOWN);
            memberEntity.setAgeRange(AgeRange.UNKNOWN);
            memberEntity.setTravelStyle(TravelStyle.UNKNOWN);
            memberEntity.setRating(rating);
            memberEntity.setAuthority(authority);
            memberEntity.setVerified(verified);
            memberEntity.setProvider(provider);
            memberEntity.setProviderId(providerId);
            memberEntity.setDeleted(false);

            memberRepository.save(memberEntity);

            // 새로운 회원 DB에 저장
            memberRepository.save(memberEntity);

            return new PrincipalDetails(memberEntity, oAuth2User.getAttributes());
        } else {
            System.out.println("로그인을 이미 한 적이 있습니다. 당신은 자동 회원가입이 되어 있습니다.");
            // 이미 존재하는 사용자라면 기본 정보를 기반으로 로그인 처리
        }

        // PrincipalDetails 반환
        return new PrincipalDetails(memberEntityOptional.orElseThrow(() -> new OAuth2AuthenticationException("Member not found")),
                oAuth2User.getAttributes());
    }

    // 랜덤 비밀번호 생성
    private static String generateRandomPassword(int length) {

        StringBuilder password = new StringBuilder(length);
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }
        return password.toString();
    }
}
