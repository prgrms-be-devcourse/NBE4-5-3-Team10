import { api } from "../utils/api";

// 리뷰 관련 타입 정의
export interface Review {
  reviewId: number;
  title: string;
  content: string;
  rating: number;
  memberId: number;
  memberName: string;
  profileImage?: string;
  placeId: number;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
  commentCount: number;
  placeName?: string;
}

export interface ReviewDetail extends Review {
  // 추가 필드가 있다면 여기에 정의
}

export interface ReviewRequestDto {
  title: string;
  content: string;
  rating: number;
  placeId: number;
}

// 리뷰 목록 조회
export async function getReviews(
  sort: string = "newest",
  keyword?: string,
  placeId?: number,
  page: number = 1
): Promise<{ reviews: Review[]; totalPages: number; currentPage: number }> {
  // sort 파라미터 매핑 - 프론트엔드 값을 백엔드 값으로 변환
  let apiSortParam = sort;

  // 백엔드 API에 맞게 정렬 파라미터 변환
  if (sort === "most_comments") {
    apiSortParam = "comments"; // 백엔드에서 예상하는 파라미터로 변경
  }

  let url = `/api/reviews?sort=${apiSortParam}&page=${page}`;

  if (keyword) {
    url += `&keyword=${encodeURIComponent(keyword)}`;
  }

  if (placeId) {
    url += `&placeId=${placeId}`;
  }

  try {
    console.log("📡 API 요청 URL:", url);

    // 직접 fetch 호출로 원본 데이터 확인
    const baseUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
    const fullUrl = `${baseUrl}${url}`;
    console.log("🔗 전체 URL:", fullUrl);

    const headers: HeadersInit = {
      "Content-Type": "application/json",
    };

    // 인증 토큰 추가
    if (typeof window !== "undefined") {
      const token = localStorage.getItem("accessToken");
      if (token) {
        headers["Authorization"] = `Bearer ${token}`;
      }
    }

    console.log("🔑 요청 헤더:", headers);

    // 직접 fetch 호출
    const response = await fetch(fullUrl, {
      method: "GET",
      headers,
      credentials: "include",
    });

    console.log("📥 응답 상태:", response.status);

    if (!response.ok) {
      throw new Error(`API 요청 실패: ${response.status}`);
    }

    // 원본 응답 데이터 가져오기
    const rawData = await response.json();
    console.log("📦 원본 응답 데이터:", rawData);

    // RsData 구조 처리
    let reviewData: Review[] = [];

    if (rawData && typeof rawData === "object") {
      if ("data" in rawData) {
        // RsData 구조 (data 필드에 실제 데이터가 있음)
        console.log("✅ RsData 구조 확인");
        reviewData = Array.isArray(rawData.data) ? rawData.data : [];
      } else if (Array.isArray(rawData)) {
        // 직접 배열 형태로 응답이 왔을 경우
        console.log("✅ 배열 구조 확인");
        reviewData = rawData;
      }
    }

    console.log("📊 최종 리뷰 데이터:", reviewData);
    console.log("🔢 리뷰 개수:", reviewData.length);

    // 페이지네이션 계산
    const totalItems = reviewData.length;
    const itemsPerPage = 6;
    const totalPages = Math.max(1, Math.ceil(totalItems / itemsPerPage));

    console.log("📄 페이지 정보:", {
      totalItems,
      itemsPerPage,
      totalPages,
      currentPage: page,
    });

    return {
      reviews: reviewData,
      totalPages,
      currentPage: page,
    };
  } catch (error) {
    console.error("❌ 리뷰 목록 조회 중 오류 발생:", error);
    // 오류 발생 시 빈 결과 반환
    return {
      reviews: [],
      totalPages: 1,
      currentPage: page,
    };
  }
}
// 인기 리뷰 조회
export async function getPopularReviews(limit: number = 10): Promise<Review[]> {
  try {
    const reviews = await api.get<Review[]>(
      `/api/reviews/popular?limit=${limit}`
    );
    return reviews || [];
  } catch (error) {
    console.error("인기 리뷰 조회 중 오류 발생:", error);
    return [];
  }
}

// 특정 여행지의 리뷰 조회
export async function getReviewsByPlace(placeId: number): Promise<Review[]> {
  try {
    const reviews = await api.get<Review[]>(`/api/reviews/place/${placeId}`);
    return reviews || [];
  } catch (error) {
    console.error(`여행지 ID ${placeId}의 리뷰 조회 중 오류 발생:`, error);
    return [];
  }
}

// 리뷰 상세 조회
export async function getReviewById(reviewId: number): Promise<ReviewDetail> {
  try {
    const review = await api.get<ReviewDetail>(`/api/reviews/${reviewId}`);
    return review;
  } catch (error) {
    console.error(`리뷰 ID ${reviewId} 조회 중 오류 발생:`, error);
    throw error;
  }
}

// 리뷰 생성
export async function createReview(review: ReviewRequestDto): Promise<Review> {
  try {
    console.log("📤 리뷰 생성 요청:", review);

    const createdReview = await api.post<Review>("/api/reviews", review);

    console.log("✅ 리뷰 생성 성공:", createdReview);
    return createdReview;
  } catch (error) {
    console.error("❌ 리뷰 생성 중 오류 발생:", error);
    throw error;
  }
}

// 리뷰 수정
export async function updateReview(
  reviewId: number,
  review: ReviewRequestDto
): Promise<Review> {
  try {
    const updatedReview = await api.put<Review>(
      `/api/reviews/${reviewId}`,
      review
    );
    return updatedReview;
  } catch (error) {
    console.error(`리뷰 ID ${reviewId} 수정 중 오류 발생:`, error);
    throw error;
  }
}

// 리뷰 삭제
export async function deleteReview(reviewId: number): Promise<void> {
  try {
    await api.delete(`/api/reviews/${reviewId}`);
  } catch (error) {
    console.error(`리뷰 ID ${reviewId} 삭제 중 오류 발생:`, error);
    throw error;
  }
}

// 내 리뷰 조회
export async function getMyReviews(): Promise<Review[]> {
  try {
    const myReviews = await api.get<Review[]>("/api/reviews/my");
    return myReviews || [];
  } catch (error) {
    console.error("내 리뷰 조회 중 오류 발생:", error);
    return [];
  }
}

// 리뷰 이미지 업로드
export async function uploadReviewImages(
  reviewId: number,
  formData: FormData
): Promise<string[]> {
  try {
    const imageUrls = await api.post<string[]>(
      `/api/reviews/${reviewId}/images`,
      formData,
      true
    );
    return imageUrls || [];
  } catch (error) {
    console.error(`리뷰 ID ${reviewId}의 이미지 업로드 중 오류 발생:`, error);
    return [];
  }
}
