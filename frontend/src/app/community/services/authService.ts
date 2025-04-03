import { api } from "../utils/api";

export interface UserInfo {
  id: number;
  username: string;
  email: string;
  nickname: string;
  profileImage?: string;
  authority: string;
}

// 현재 로그인 상태 확인
export function isLoggedIn(): boolean {
  if (typeof window !== "undefined") {
    const token = localStorage.getItem("accessToken");
    console.log(`🔒 로그인 상태 확인: ${token ? "로그인됨" : "로그아웃됨"}`);
    return !!token;
  }
  return false;
}

// 현재 사용자 정보 가져오기
export async function getCurrentUserInfo(): Promise<UserInfo | null> {
  if (!isLoggedIn()) {
    return null;
  }

  try {
    const userInfo = await api.get<UserInfo>("/member/mypage");
    return userInfo || null;
  } catch (error) {
    console.error("사용자 정보 조회 중 오류 발생:", error);
    return null;
  }
}

// 로그아웃
export async function logout(): Promise<boolean> {
  try {
    await api.post("/member/logout");
    if (typeof window !== "undefined") {
      localStorage.removeItem("accessToken");
      // 사용자 정의 이벤트 발생
      window.dispatchEvent(new Event("logout"));
    }
    return true;
  } catch (error) {
    console.error("로그아웃 중 오류 발생:", error);
    if (typeof window !== "undefined") {
      localStorage.removeItem("accessToken");
    }
    return false;
  }
}

// 액세스 토큰 갱신
export async function refreshToken(): Promise<string | null> {
  try {
    const response = await api.post<{ accessToken: string }>("/member/refresh");

    if (response && response.accessToken) {
      if (typeof window !== "undefined") {
        localStorage.setItem("accessToken", response.accessToken);
      }
      return response.accessToken;
    }
    return null;
  } catch (error) {
    console.error("토큰 갱신 중 오류 발생:", error);
    return null;
  }
}
