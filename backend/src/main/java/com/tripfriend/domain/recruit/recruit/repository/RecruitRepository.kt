package com.tripfriend.domain.recruit.recruit.repository

import com.tripfriend.domain.recruit.recruit.entity.Recruit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RecruitRepository : JpaRepository<Recruit, Long>, RecruitRepositoryCustom {
    fun findAllByOrderByCreatedAtDesc(): List<Recruit>
    fun findTop3ByOrderByCreatedAtDesc(): List<Recruit>
}