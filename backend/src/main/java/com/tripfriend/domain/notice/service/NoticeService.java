package com.tripfriend.domain.notice.service;

import com.tripfriend.domain.notice.entity.Notice;
import com.tripfriend.domain.notice.repository.NoticeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;

    //공지사항 저장
    public Notice createNotice(String title, String content) {
        Notice notice = Notice.builder()
                .title(title)
                .content(content)
                .build();
        return noticeRepository.save(notice);
    }

    //공지사항 전체 조회
    public List<Notice> getAllNotices() {
        return noticeRepository.findAll();
    }

    //공지사항 검색
    public Notice getNoticeById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("해당 공지사항을 찾을 수 없습니다."));
    }

    //공지사항 수정
    @Transactional
    public Notice updateNoticeById(Long id, String title, String content) {
        Notice notice = getNoticeById(id);
        notice.update(title,content);
        return notice;

    }


    //공지사항 삭제
    public void deleteNotice(Long id){
        Notice notice = getNoticeById(id);
        noticeRepository.deleteById(id);

    }

}
