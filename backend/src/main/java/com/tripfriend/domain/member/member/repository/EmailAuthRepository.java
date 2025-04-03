package com.tripfriend.domain.member.member.repository;

import com.tripfriend.domain.member.member.entity.EmailAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EmailAuthRepository extends JpaRepository<EmailAuth, String> {

    Optional<EmailAuth> findByEmail(String email);

    @Modifying
    @Query("DELETE FROM EmailAuth e WHERE e.expireAt < CURRENT_TIMESTAMP")
    void deleteExpiredCodes();
}
