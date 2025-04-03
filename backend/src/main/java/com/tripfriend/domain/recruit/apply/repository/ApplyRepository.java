package com.tripfriend.domain.recruit.apply.repository;

import com.tripfriend.domain.recruit.apply.entity.Apply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplyRepository extends JpaRepository<Apply, Long> { // extends 해줘야함
    List<Apply> findByRecruitRecruitId(Long recruitId);
}
