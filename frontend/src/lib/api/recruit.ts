const API_BASE_URL = `${process.env.NEXT_PUBLIC_API_URL}/recruits`; // 로컬 백엔드 URL

export async function getRecruits() {
  const response = await fetch(API_BASE_URL);
  if (!response.ok) throw new Error("모집 목록을 불러오지 못했습니다.");
  return response.json();
}

export async function getRecruitById(recruitId: string) {
  const response = await fetch(
    `${process.env.NEXT_PUBLIC_API_URL}/recruits/${recruitId}`
  );

  if (!response.ok) {
    throw new Error("❌ 모집 상세 정보를 불러오지 못했습니다.");
  }

  return response.json();
}

export async function searchAndFilterRecruits(params: Record<string, any>) {
  // 🔹 localStorage에서 토큰 가져오기
  const token =
    typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;

  const filteredParams = Object.fromEntries(
    Object.entries(params).filter(
      ([_, value]) => value !== undefined && value !== ""
    )
  );

  const queryString = new URLSearchParams(filteredParams).toString();

  const res = await fetch(
    `${process.env.NEXT_PUBLIC_API_URL}/recruits/search3?${queryString}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        ...(token && { Authorization: `Bearer ${token}` }), // ✅ 토큰을 Authorization 헤더에 추가
      },
    }
  );

  if (!res.ok) {
    throw new Error(`API 요청 실패: ${res.status}`);
  }

  return res.json();
}

// export async function searchAndFilterRecruits(params: Record<string, any>) {
//   // 저장된 토큰 가져오기
//   const token =
//     typeof window !== "undefined" ? localStorage.getItem("token") : null;

//   const filteredParams = Object.fromEntries(
//     Object.entries(params).filter(
//       ([_, value]) => value !== undefined && value !== ""
//     )
//   );

//   const queryString = new URLSearchParams(filteredParams).toString();

//   const res = await fetch(
//     `${process.env.NEXT_PUBLIC_API_URL}/recruits/search3?${queryString}`,
//     {
//       method: "GET",
//       headers: {
//         "Content-Type": "application/json",
//         ...(token && { Authorization: `Bearer ${token}` }), // 토큰이 있으면 Authorization 헤더 추가
//       },
//     }
//   );

//   if (!res.ok) {
//     throw new Error(`API 요청 실패: ${res.status}`);
//   }

//   return res.json();
// }
