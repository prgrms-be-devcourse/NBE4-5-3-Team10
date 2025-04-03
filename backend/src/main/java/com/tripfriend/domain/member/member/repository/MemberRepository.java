package com.tripfriend.domain.member.member.repository;

import com.tripfriend.domain.member.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<Member> findByUsername(String username);

    Optional<Member> findByEmail(String email);

    // 삭제된 계정만 조회
    Optional<Member> findByIdAndDeletedTrue(Long id);

    // 이메일과 삭제 여부로 조회 (로그인에서 활용)
    Optional<Member> findByEmailAndDeleted(String email, boolean deleted);

    // 삭제된 계정 중 특정 기간 내의 계정들 조회
    List<Member> findByDeletedTrueAndDeletedAtAfter(LocalDateTime date);

    // 삭제된 계정 중 해당 날짜 이전에 삭제된 계정들 조회
    List<Member> findByDeletedTrueAndDeletedAtBefore(LocalDateTime date);
}
