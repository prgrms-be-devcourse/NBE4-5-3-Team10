package com.tripfriend.domain.review.repository

import com.tripfriend.domain.review.entity.ReviewViewCount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReviewViewCountRepository : JpaRepository<ReviewViewCount, Long>