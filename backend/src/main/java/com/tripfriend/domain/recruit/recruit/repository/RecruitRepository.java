package com.tripfriend.domain.recruit.recruit.repository;

import com.tripfriend.domain.recruit.recruit.entity.Recruit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecruitRepository extends JpaRepository<Recruit, Long>, RecruitRepositoryCustom {
    List<Recruit> findAllByOrderByCreatedAtDesc();
    List<Recruit> findTop3ByOrderByCreatedAtDesc();
}
