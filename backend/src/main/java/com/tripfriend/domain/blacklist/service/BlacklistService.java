package com.tripfriend.domain.blacklist.service;

import com.tripfriend.domain.blacklist.entity.Blacklist;
import com.tripfriend.domain.blacklist.repository.BlacklistRepository;
import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlacklistService {
    private final BlacklistRepository blacklistRepository;
    private final MemberRepository memberRepository;

    //블랙리스트 추가
    @Transactional
    public void addToBlacklist(Long memberId, String reason) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        if (blacklistRepository.existsByMember(member)) {
            throw new IllegalArgumentException("이미 블랙리스트에 등록된 회원입니다.");
        }
        Blacklist blacklist = Blacklist.builder()
                .member(member)
                .reason(reason)
                .build();
        blacklistRepository.save(blacklist);

    }
    //블랙리스트 삭제
    @Transactional
    public void removeFromBlacklist(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        Blacklist blacklist = blacklistRepository.findByMember(member)
                .orElseThrow(() -> new IllegalArgumentException("블랙리스트에 등록되지 않은 회원입니다."));

        blacklistRepository.delete(blacklist);
    }

    // 블랙리스트 조회
    public List<Blacklist> getBlacklist() {
        return blacklistRepository.findAll();
    }

}
