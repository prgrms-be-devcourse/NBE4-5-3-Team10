import api from "@/lib/api";
import { Notice } from "@/app/admin/notice/page";



// 관리자 전용 전체 조회 (토큰 필요)
export const fetchAllNoticesForAdmin = async (): Promise<Notice[]> => {
  const token = localStorage.getItem("accessToken");
  const res = await api.get("/notice/admin", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  return res.data;
};

// 일반 사용자용 공지사항 조회 (비로그인 허용)
export const fetchAllNoticesPublic = async (): Promise<Notice[]> => {
  const res = await api.get("/notice"); // 인증 없이 호출
  return res.data;
};

// 공지 생성
export const createNotice = async (notice: { title: string; content: string }) => {
  const token = localStorage.getItem("accessToken");
  return api.post("/notice/admin", notice, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};

// 공지 수정
export const updateNotice = async (
  id: number,
  notice: { title: string; content: string }
) => {
  const token = localStorage.getItem("accessToken");
  return api.put(`/notice/admin/${id}`, notice, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};

// 공지 삭제
export const deleteNotice = async (id: number) => {
  const token = localStorage.getItem("accessToken");
  return api.delete(`/notice/admin/${id}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};
