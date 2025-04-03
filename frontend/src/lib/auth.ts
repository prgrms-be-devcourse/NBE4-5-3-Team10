export const fetchWithAuth = async (url: string, options: RequestInit = {}) => {
  let token = localStorage.getItem("accessToken");

  // 기본 요청 옵션 설정
  const fetchOptions: RequestInit = {
    ...options,
    headers: {
      ...options.headers,
      "Content-Type": "application/json",
    },
  };

  // 토큰이 존재하면 Authorization 헤더 추가
  if (token) {
    fetchOptions.headers = {
      ...fetchOptions.headers,
      Authorization: `Bearer ${token}`,
    };
  }

  const response = await fetch(url, fetchOptions);

  // 액세스 토큰 만료 시 자동 갱신 처리
  if (response.status === 401 && token) {
    console.warn("🔄 액세스 토큰 만료됨, 리프레시 시도 중...");
    token = await refreshAccessToken();

    if (!token) {
      console.warn("🚫 리프레시 실패, 로그인 필요.");
      return response; // 로그인 필요 시 기존 응답 그대로 반환
    }

    // 새로운 토큰으로 재시도
    return fetch(url, {
      ...options,
      headers: {
        ...options.headers,
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });
  }

  return response;
};

export const refreshAccessToken = async () => {
  try {
    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/member/refresh`,
      {
        method: "POST",
        credentials: "include",
      }
    );

    if (!response.ok) throw new Error("토큰 갱신 실패");

    const data = await response.json();
    localStorage.setItem("accessToken", data.accessToken);
    return data.accessToken;
  } catch (error) {
    console.error("❌ 액세스 토큰 갱신 오류:", error);
    return null;
  }
};
