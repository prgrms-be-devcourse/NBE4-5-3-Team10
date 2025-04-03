// API 기본 URL 설정
export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

// API 요청 타입 정의
type HttpMethod = "GET" | "POST" | "PUT" | "DELETE";

// 인증 헤더 가져오기
const getAuthHeader = (): Record<string, string> => {
  if (typeof window !== "undefined") {
    const token = localStorage.getItem("accessToken");
    // 토큰 디버깅
    console.log(`🔑 인증 토큰 ${token ? "있음" : "없음"}`);
    return token ? { Authorization: `Bearer ${token}` } : {};
  }
  return {};
};

// 기본 API 요청 함수
export async function apiRequest<T>(
  url: string,
  method: HttpMethod = "GET",
  data?: any,
  isFormData: boolean = false
): Promise<T> {
  const headers: HeadersInit = {
    ...getAuthHeader(),
  };

  // FormData가 아닌 경우에만 Content-Type 헤더 추가
  if (!isFormData && method !== "GET") {
    headers["Content-Type"] = "application/json";
  }

  const config: RequestInit = {
    method,
    headers,
    credentials: "include", // 쿠키 포함
  };

  // GET 요청이 아니고 데이터가 있는 경우
  if (method !== "GET" && data) {
    config.body = isFormData ? data : JSON.stringify(data);
  }

  try {
    // API 요청 URL 로깅 (디버깅용)
    console.log(`🔄 API 요청: ${method} ${API_BASE_URL}${url}`);
    if (data && !isFormData) console.log("📦 요청 데이터:", data);

    const response = await fetch(`${API_BASE_URL}${url}`, config);
    console.log(`📤 응답 상태: ${response.status}`);

    // 401 Unauthorized - 토큰 만료 처리
    if (response.status === 401) {
      // 리프레시 토큰으로 새 액세스 토큰 요청
      try {
        const refreshResponse = await fetch(`${API_BASE_URL}/member/refresh`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          credentials: "include",
        });

        if (refreshResponse.ok) {
          const refreshData = await refreshResponse.json();

          // 새 액세스 토큰 저장
          if (typeof window !== "undefined") {
            localStorage.setItem("accessToken", refreshData.data.accessToken);
          }

          // 원래 요청 재시도
          headers["Authorization"] = `Bearer ${refreshData.data.accessToken}`;
          const retryResponse = await fetch(`${API_BASE_URL}${url}`, {
            ...config,
            headers,
          });

          if (!retryResponse.ok) {
            throw new Error(`API 요청 실패: ${retryResponse.status}`);
          }

          return processResponse<T>(retryResponse);
        } else {
          // 리프레시 토큰도 만료된 경우 로그아웃 처리
          if (typeof window !== "undefined") {
            localStorage.removeItem("accessToken");
            window.location.href = "/member/login";
          }
          throw new Error("인증이 만료되었습니다. 다시 로그인해주세요.");
        }
      } catch (error) {
        // 리프레시 토큰 요청 자체가 실패한 경우
        if (typeof window !== "undefined") {
          localStorage.removeItem("accessToken");
          window.location.href = "/member/login";
        }
        throw new Error("인증 갱신에 실패했습니다. 다시 로그인해주세요.");
      }
    }

    if (!response.ok) {
      // 에러 응답을 좀 더 상세히 처리
      const errorText = await response.text();
      try {
        const errorJson = JSON.parse(errorText);
        throw new Error(
          `API 오류: ${errorJson.msg || errorJson.message || response.status}`
        );
      } catch (e) {
        throw new Error(
          `API 요청 실패: ${response.status} - ${
            errorText || "알 수 없는 오류"
          }`
        );
      }
    }

    return processResponse<T>(response);
  } catch (error) {
    console.error("❌ API 요청 중 오류 발생:", error);
    throw error;
  }
}

// 응답 처리 함수
async function processResponse<T>(response: Response): Promise<T> {
  console.log("🔄 processResponse 시작");
  console.log("📤 응답 상태:", response.status);

  try {
    const contentType = response.headers.get("content-type");
    console.log("📋 Content-Type:", contentType);

    // JSON 응답 처리
    if (contentType && contentType.includes("application/json")) {
      console.log("🔍 JSON 응답 감지");

      // JSON 파싱
      let rawData;
      try {
        rawData = await response.json();
        console.log("📦 원본 JSON 데이터:", rawData);
      } catch (jsonError) {
        console.error("❌ JSON 파싱 오류:", jsonError);
        throw new Error("JSON 파싱 실패");
      }

      // RsData 구조 확인 (code, msg, data 필드 있는지)
      if (rawData && typeof rawData === "object") {
        console.log("🔑 응답 키들:", Object.keys(rawData));

        const hasRsDataStructure = "code" in rawData && "msg" in rawData;
        console.log("🧪 RsData 구조 여부:", hasRsDataStructure);

        if (hasRsDataStructure) {
          // 오류 코드인 경우 예외 발생
          if (rawData.code.startsWith("4") || rawData.code.startsWith("5")) {
            console.error("🚫 API 오류:", rawData.code, rawData.msg);
            throw new Error(rawData.msg || "서버 오류가 발생했습니다.");
          }

          // data 필드가 있는지 확인
          if ("data" in rawData) {
            const dataType = Array.isArray(rawData.data)
              ? "배열"
              : rawData.data === null
              ? "null"
              : typeof rawData.data;
            console.log("📎 data 필드 타입:", dataType);

            if (rawData.data === null) {
              console.log("⚠️ data 필드가 null입니다.");
              // 배열 타입인 경우 빈 배열 반환
              if (Array.isArray({} as unknown as T)) {
                console.log("🔄 빈 배열 반환");
                return [] as unknown as T;
              }
              console.log("🔄 빈 객체 반환");
              return {} as unknown as T;
            }

            console.log("✅ data 필드 반환");
            return rawData.data as T;
          } else {
            console.warn("⚠️ data 필드가 없습니다.");
            // 배열 타입인 경우 빈 배열 반환
            if (Array.isArray({} as unknown as T)) {
              console.log("🔄 빈 배열 반환");
              return [] as unknown as T;
            }
            console.log("🔄 빈 객체 반환");
            return {} as unknown as T;
          }
        }
      }

      // RsData 형식이 아닌 경우 전체 데이터 반환
      console.log("🔄 원본 데이터 반환");
      return rawData as T;
    }

    // 텍스트 응답 처리
    if (contentType && contentType.includes("text/plain")) {
      const text = await response.text();
      console.log("📝 텍스트 응답:", text);
      return text as unknown as T;
    }

    // 기타 응답 형식
    console.warn("⚠️ 지원되지 않는 응답 형식:", contentType);

    // 빈 응답 반환 (타입에 따라)
    if (Array.isArray({} as unknown as T)) {
      console.log("🔄 타입이 배열이므로 빈 배열 반환");
      return [] as unknown as T;
    }

    console.log("🔄 빈 객체 반환");
    return {} as unknown as T;
  } catch (error) {
    console.error("❌ 응답 처리 중 오류 발생:", error);
    throw error;
  } finally {
    console.log("🔄 processResponse 완료");
  }
}
// 편의 함수
export const api = {
  get: <T>(url: string) => apiRequest<T>(url, "GET"),
  post: <T>(url: string, data?: any, isFormData: boolean = false) =>
    apiRequest<T>(url, "POST", data, isFormData),
  put: <T>(url: string, data?: any, isFormData: boolean = false) =>
    apiRequest<T>(url, "PUT", data, isFormData),
  delete: <T>(url: string) => apiRequest<T>(url, "DELETE"),
};
