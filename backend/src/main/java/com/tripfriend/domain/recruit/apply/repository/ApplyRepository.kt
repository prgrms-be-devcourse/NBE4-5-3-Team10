package com.tripfriend.domain.recruit.apply.repository

import com.tripfriend.domain.recruit.apply.entity.Apply
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplyRepository : JpaRepository<Apply, Long> {
}
