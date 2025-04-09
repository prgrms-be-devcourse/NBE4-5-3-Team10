package com.tripfriend.domain.notice.repository

import com.tripfriend.domain.notice.entity.Notice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NoticeRepository : JpaRepository<Notice, Long>
