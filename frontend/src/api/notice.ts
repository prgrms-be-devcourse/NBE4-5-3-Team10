import api from "@/lib/api";


export type Notice = {
    id: number;
    title: string;
    content: string;
    createdAt: string;
    updatedAt: string;
  };

  export const login = async (username: string, password: string) => {
    const res = await api.post("/member/login", {
      username,
      password,
    });
  
    const { accessToken, refreshToken } = res.data;
  
    // localStorage에 저장
    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("refreshToken", refreshToken);
  };

  
const getToken = () => {
  if (typeof window !== "undefined") {
    return localStorage.getItem("accessToken");
  }
  return null;
};

// 전체 조회
export const fetchAllNotices = async (): Promise<Notice[]> => {
  const token = getToken();
  const res = await api.get("/admin/notice", {
    headers: { Authorization: `Bearer ${token}` },
  });
  return res.data;
};

// 생성
export const createNotice = async (data: { title: string; content: string }): Promise<void> => {
  const token = getToken();
  await api.post("/admin/notice", data, {
    headers: { Authorization: `Bearer ${token}` },
  });
};

// 수정
export const updateNotice = async (id: number, data: { title: string; content: string }): Promise<void> => {
  const token = getToken();
  console.log("수정 요청 데이터:", data);
  await api.put(`/admin/notice/${id}`, data, {
    headers: { Authorization: `Bearer ${token}` },
  });
};

// 삭제
export const deleteNotice = async (id: number): Promise<void> => {
  const token = getToken();
  await api.delete(`/admin/notice/${id}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
};
