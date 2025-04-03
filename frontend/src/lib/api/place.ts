import axios from "axios";

// const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL; // 환경변수에서 API 주소 가져오기

// 도시 목록 불러오는 API 함수
export async function getCities() {
  try {
    const response = await axios.get(
      `${process.env.NEXT_PUBLIC_API_URL}/place/cities`
    );
    return response.data; // RsData<List<String>> 형태의 응답 반환
  } catch (error) {
    console.error("도시 목록 조회 실패:", error);
    throw new Error("도시 목록을 불러오는 중 오류가 발생했습니다.");
  }
}
