package com.tripfriend.domain.member.member.service;

import com.tripfriend.domain.member.member.dto.JoinRequestDto;
import com.tripfriend.domain.member.member.dto.MemberResponseDto;
import com.tripfriend.domain.member.member.dto.MemberUpdateRequestDto;
import com.tripfriend.domain.member.member.entity.AgeRange;
import com.tripfriend.domain.member.member.entity.Gender;
import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.entity.TravelStyle;
import com.tripfriend.domain.member.member.repository.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MailService mailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    private JoinRequestDto joinRequestDto;

    private MemberUpdateRequestDto memberUpdateRequestDto;

    private Member member;

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        joinRequestDto = JoinRequestDto.builder()
                .username("testUser")
                .email("test@example.com")
                .password("password123")
                .nickname("testNickname")
                .gender(Gender.MALE)
                .ageRange(AgeRange.TWENTIES)
                .travelStyle(TravelStyle.TOURISM)
                .build();

        memberUpdateRequestDto = MemberUpdateRequestDto.builder()
                .email("new@example.com")
                .nickname("newNickname")
                .password("newPassword123")
                .profileImage("newProfileImageUrl")
                .gender(Gender.FEMALE)
                .ageRange(AgeRange.THIRTIES)
                .travelStyle(TravelStyle.SHOPPING)
                .aboutMe("Updated about me")
                .build();

        member = joinRequestDto.toEntity();
        member.setPassword("encryptedPassword");
    }

    @Test
    @DisplayName("회원 가입")
    void joins() throws MessagingException {

        // Given
        when(memberRepository.existsByUsername(joinRequestDto.getUsername())).thenReturn(false);
        when(memberRepository.existsByEmail(joinRequestDto.getEmail())).thenReturn(false);
        when(memberRepository.existsByNickname(joinRequestDto.getNickname())).thenReturn(false);
        when(passwordEncoder.encode(joinRequestDto.getPassword())).thenReturn("encryptedPassword");
        when(mailService.sendAuthCode(anyString())).thenReturn(true);

        Member member = joinRequestDto.toEntity();
        member.setPassword("encryptedPassword");
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        MemberResponseDto responseDto = memberService.join(joinRequestDto);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getUsername()).isEqualTo(joinRequestDto.getUsername());
    }

    @Test
    @DisplayName("회원 정보 수정")
    void updateMember() {

        // Given
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(memberRepository.existsByEmail(memberUpdateRequestDto.getEmail())).thenReturn(false);  // 이메일 중복 없음
        when(memberRepository.existsByNickname(memberUpdateRequestDto.getNickname())).thenReturn(false);  // 닉네임 중복 없음
        when(passwordEncoder.encode(memberUpdateRequestDto.getPassword())).thenReturn("encryptedNewPassword");
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        MemberResponseDto responseDto = memberService.updateMember(1L, memberUpdateRequestDto);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getEmail()).isEqualTo(memberUpdateRequestDto.getEmail());
        assertThat(responseDto.getNickname()).isEqualTo(memberUpdateRequestDto.getNickname());
        assertThat(responseDto.getProfileImage()).isEqualTo(memberUpdateRequestDto.getProfileImage());
        assertThat(responseDto.getGender()).isEqualTo(memberUpdateRequestDto.getGender());
        assertThat(responseDto.getAgeRange()).isEqualTo(memberUpdateRequestDto.getAgeRange());
        assertThat(responseDto.getTravelStyle()).isEqualTo(memberUpdateRequestDto.getTravelStyle());
        assertThat(responseDto.getAboutMe()).isEqualTo(memberUpdateRequestDto.getAboutMe());
        assertThat(responseDto.getRating()).isEqualTo(member.getRating());  // Rating 체크
        assertThat(responseDto.getCreatedAt()).isEqualTo(member.getCreatedAt());  // createdAt 체크
        assertThat(responseDto.getUpdatedAt()).isEqualTo(member.getUpdatedAt());  // updatedAt 체크
        assertThat(responseDto.getAuthority()).isEqualTo(member.getAuthority());  // authority 체크
    }

    @Test
    @DisplayName("회원 삭제")
    void deleteMember() {

        // Given
        Long memberId = 1L;
        when(memberRepository.existsById(memberId)).thenReturn(true);
        doNothing().when(memberRepository).deleteById(memberId);

        // When
        memberService.deleteMember(memberId,null,null);

        // Then
        verify(memberRepository, times(1)).existsById(memberId);
        verify(memberRepository, times(1)).deleteById(memberId);
    }
}