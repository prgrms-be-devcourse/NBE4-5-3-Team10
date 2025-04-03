"use client";
import { useState, useEffect } from "react";
import Link from "next/link";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import Place from "./place/ClientPage"; // 여행지 조회
import { fetchWithAuth } from "@/lib/auth"; // 인증된 요청을 위한 유틸

const API_BASE_URL = `${process.env.NEXT_PUBLIC_API_URL}/recruits`;

export default function Home() {
  const [searchQuery, setSearchQuery] = useState("");
  const [destination, setDestination] = useState("");
  const [recentTrips, setRecentTrips] = useState([]); // 🔹 최신 모집글 데이터 저장

  // 모집 글 타입 정의
  interface recentTrips {
    recruitId: number;
    memberProfileImage: string;
    memberNickname: string;
    placeCityName: string;
    placePlaceName: string;
    title: string;
    isClosed: boolean;
    startDate: string;
    endDate: string;
    travelStyle: string;
    genderRestriction: string;
    ageRestriction: string;
    budget: number;
    groupSize: number;
    createdAt: string;
    updatedAt: string;
  }

  // // 인기 여행지 데이터
  // const popularDestinations = [
  //   { id: 1, name: "제주도", image: "/placeholder.jpg", tripCount: 243 },
  //   { id: 2, name: "방콕", image: "/placeholder.jpg", tripCount: 187 },
  //   { id: 3, name: "오사카", image: "/placeholder.jpg", tripCount: 156 },
  //   { id: 4, name: "파리", image: "/placeholder.jpg", tripCount: 142 },
  // ];

  // 최근 등록된 여행 동행 데이터
  // const recentTrips = [
  //   {
  //     id: 101,
  //     title: "제주도 4박 5일 같이 여행하실 분",
  //     destination: "제주도",
  //     date: "2025-04-10 ~ 2025-04-14",
  //     companions: 2,
  //   },
  //   {
  //     id: 102,
  //     title: "태국 방콕 맛집 투어 동행 구해요",
  //     destination: "방콕",
  //     date: "2025-05-15 ~ 2025-05-20",
  //     companions: 1,
  //   },
  //   {
  //     id: 103,
  //     title: "오사카 2박 3일 여행 파트너 찾습니다",
  //     destination: "오사카",
  //     date: "2025-04-22 ~ 2025-04-24",
  //     companions: 3,
  //   },
  // ];

  useEffect(() => {
    async function fetchRecentTrips() {
      try {
        const response = await fetchWithAuth(`${API_BASE_URL}/recent3`);
        if (!response.ok)
          throw new Error("최근 모집글을 불러오는 데 실패했습니다.");
        const data = await response.json();
        console.log(data.data);
        setRecentTrips(data.data);
      } catch (error) {
        console.error("❌ 모집글 데이터 불러오기 오류:", error);
      }
    }

    fetchRecentTrips();
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    // 실제 검색 기능 구현 부분
    console.log("검색:", destination, searchQuery);
    // 검색 결과 페이지로 리다이렉트 기능 추가 예정
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 헤더 컴포넌트 사용 */}
      <Header />

      {/* 히어로 섹션 */}
      <div className="relative bg-blue-600 text-white">
        <div className="container mx-auto px-4 py-16 md:py-24">
          <div className="max-w-2xl">
            <h2 className="text-3xl md:text-5xl font-bold mb-6">
              혼자 떠나는 여행
            </h2>
            <h2 className="text-3xl md:text-5xl font-bold mb-6">
              함께할 동행을 찾아보세요
            </h2>
            <p className="text-xl mb-8">
              전 세계 여행자들과 함께하는 특별한 여행 경험을 만들어보세요.
            </p>

            {/* 검색 폼 */}
            <form
              onSubmit={handleSearch}
              className="bg-white p-4 rounded-lg shadow-lg flex flex-col md:flex-row"
            >
              <select
                className="mb-2 md:mb-0 p-3 bg-gray-100 rounded-md md:w-1/3 md:mr-2 text-gray-800"
                value={destination}
                onChange={(e) => setDestination(e.target.value)}
              >
                <option value="">여행지 선택</option>
                <option value="국내">국내</option>
                <option value="아시아">아시아</option>
                <option value="유럽">유럽</option>
                <option value="미주">미주</option>
                <option value="오세아니아">오세아니아</option>
              </select>
              <input
                type="text"
                placeholder="검색어를 입력하세요"
                className="mb-2 md:mb-0 p-3 bg-gray-100 rounded-md md:flex-grow md:mx-2 text-gray-800"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              <button
                type="submit"
                className="p-3 bg-blue-700 text-white rounded-md hover:bg-blue-800"
              >
                검색하기
              </button>
            </form>
          </div>
        </div>
      </div>

      {/* 인기 여행지 섹션 */}
      <div className="container mx-auto px-4 py-12">
        {/* <h3 className="text-2xl font-bold mb-6">인기 여행지</h3> */}
        <Place />
      </div>

      {/* 최근 등록된 여행 동행 */}
      <div className="bg-gray-100 py-12">
        <div className="container mx-auto px-4">
          <div className="flex justify-between items-center mb-6">
            <h3 className="text-2xl font-bold">최근 등록된 여행 동행</h3>
            <Link
              href="/recruit/list"
              className="text-blue-600 hover:underline"
            >
              모두 보기
            </Link>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {recentTrips.map((trip: recentTrips) => (
              <div
                key={trip.recruitId}
                className="bg-white rounded-lg shadow-md p-6"
              >
                <h4 className="text-lg font-semibold mb-2">{trip.title}</h4>
                <div className="mb-4">
                  <p className="text-gray-600 mb-1">
                    여행지: {trip.placePlaceName}
                  </p>
                  <p className="text-gray-600 mb-1">날짜: {trip.startDate}</p>
                  <p className="text-gray-600">모집인원: {trip.groupSize}명</p>
                </div>
                <Link
                  href={`/recruit/${trip.recruitId}`}
                  className="inline-block bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
                >
                  자세히 보기
                </Link>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* 서비스 특징 섹션 */}
      <div className="container mx-auto px-4 py-12">
        <h3 className="text-2xl font-bold text-center mb-12">
          TripFriend의 특별한 점
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="text-center">
            <div className="bg-blue-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-8 w-8 text-blue-600"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                />
              </svg>
            </div>
            <h4 className="text-xl font-semibold mb-2">믿을 수 있는 동행</h4>
            <p className="text-gray-600">
              검증된 회원들과 함께하는 안전한 여행 동행 시스템을 제공합니다.
            </p>
          </div>
          <div className="text-center">
            <div className="bg-blue-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-8 w-8 text-blue-600"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7"
                />
              </svg>
            </div>
            <h4 className="text-xl font-semibold mb-2">다양한 여행 스타일</h4>
            <p className="text-gray-600">
              취향과 관심사에 맞는 다양한 여행 스타일의 동행을 찾을 수 있습니다.
            </p>
          </div>
          <div className="text-center">
            <div className="bg-blue-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-8 w-8 text-blue-600"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
            <h4 className="text-xl font-semibold mb-2">실시간 커뮤니케이션</h4>
            <p className="text-gray-600">
              빠르고 편리한 실시간 메시지 기능으로 여행 계획을 함께 조율할 수
              있습니다.
            </p>
          </div>
        </div>
      </div>

      {/* 가입 유도 섹션 */}
      <div className="bg-blue-600 text-white py-16">
        <div className="container mx-auto px-4 text-center">
          <h3 className="text-3xl font-bold mb-6">
            지금 TripFriend와 함께 특별한 여행을 시작하세요
          </h3>
          <p className="text-xl mb-8 max-w-2xl mx-auto">
            전 세계 여행자들과 함께하는 새로운 경험, 지금 바로 무료로
            시작해보세요.
          </p>
          <Link
            href="/signup"
            className="bg-white text-blue-600 px-8 py-3 rounded-md text-lg font-semibold hover:bg-gray-100"
          >
            무료 회원가입
          </Link>
        </div>
      </div>

      {/* 푸터 컴포넌트 사용 */}
      <Footer />
    </div>
  );
}
