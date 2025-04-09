"use client";
import { useState, useEffect, useRef } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import Schedule from "./schedule/ClientPage";

// Backend DTO에 맞춘 사용자 정보 타입 정의
interface MemberResponseDto {
  id: number;
  username: string;
  email: string;
  nickname: string;
  profileImage: string;
  gender: string;
  ageRange: string;
  travelStyle: string;
  aboutMe: string;
  rating: number;
  createdAt: string;
  updatedAt: string;
  authority: string;
}

// RsData 응답 형식 정의
interface RsData<T> {
  code: string;
  msg: string;
  data: T;
}

// 사용자 여행 정보 타입 정의
interface TripInfo {
  id: number;
  title: string;
  destination: string;
  date: string;
  status: "준비중" | "여행중" | "완료";
  companions: number;
}

export default function ClientPage() {
  // 사용자 프로필 상태
  const [userProfile, setUserProfile] = useState<MemberResponseDto>({
    id: 1,
    username: "honggildong",
    email: "user@example.com",
    nickname: "홍길동",
    profileImage: "/defaultUser.png",
    gender: "MALE",
    ageRange: "TWENTIES",
    travelStyle: "ADVENTURE",
    aboutMe:
      "여행을 좋아하는 20대 직장인입니다. 맛집 탐방과 현지 문화 체험을 즐겨요!",
    rating: 4.5,
    createdAt: "2024-01-15T12:00:00",
    updatedAt: "2024-02-20T15:30:00",
    authority: "ROLE_USER",
  });

  // 내가 만든 여행 목록
  const [myTrips, setMyTrips] = useState<TripInfo[]>([
    {
      id: 201,
      title: "제주도 4박 5일 힐링 여행",
      destination: "제주도",
      date: "2025-05-10 ~ 2025-05-14",
      status: "준비중",
      companions: 2,
    },
    {
      id: 202,
      title: "오사카 벚꽃 여행",
      destination: "오사카",
      date: "2025-04-03 ~ 2025-04-06",
      status: "준비중",
      companions: 1,
    },
  ]);

  // 참여 중인 여행 목록
  const [participatingTrips, setParticipatingTrips] = useState<TripInfo[]>([
    {
      id: 301,
      title: "방콕 3박 4일 맛집 투어",
      destination: "방콕",
      date: "2025-06-20 ~ 2025-06-23",
      status: "준비중",
      companions: 3,
    },
  ]);

  // 완료된 여행 목록
  const [completedTrips, setCompletedTrips] = useState<TripInfo[]>([
    {
      id: 401,
      title: "파리 일주일 여행",
      destination: "파리",
      date: "2024-12-23 ~ 2024-12-29",
      status: "완료",
      companions: 2,
    },
    {
      id: 402,
      title: "부산 주말 여행",
      destination: "부산",
      date: "2024-11-09 ~ 2024-11-10",
      status: "완료",
      companions: 4,
    },
  ]);

  const router = useRouter();

  // 프로필 수정 모드 상태
  const [isEditing, setIsEditing] = useState(false);
  // 프로필 수정용 임시 상태
  const [editedProfile, setEditedProfile] = useState({ ...userProfile });

  // 탭 상태 관리
  const [activeTab, setActiveTab] = useState("profile");

  // 회원 탈퇴 확인 모달 상태
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  // 프로필 이미지 부분 수정
  const [imageUrl, setImageUrl] = useState("/defaultUser.png"); // 기본 이미지 설정

  // 이미지 관련 상태 추가

  const [imageLoading, setImageLoading] = useState(false);
  const [imageFile, setImageFile] = useState(null);
  const fileInputRef = useRef(null);

  // 이미지 URL 설정 함수
  const getProfileImageUrl = (imagePath) => {
    if (!imagePath) return "/defaultUser.png";

    // 백엔드 서버 URL
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

    // 이미 완전한 URL인 경우 그대로 사용
    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
      return imagePath;
    }

    // 이미지 경로가 /images/로 시작하면 API URL과 결합
    if (imagePath.startsWith("/images/")) {
      return `${apiUrl}${imagePath}`;
    }

    // 그 외의 경우 images 디렉토리에 있다고 가정
    return `${apiUrl}/images/${imagePath}`;
  };

  // 프로필 수정 모드 진입 핸들러
  const handleEditProfile = () => {
    // 수정 모드로 들어갈 때 현재 프로필 데이터로 초기화하되, null/undefined 값을 빈 문자열로 처리
    const sanitizedProfile = {
      ...userProfile,
      nickname: userProfile.nickname || "",
      email: userProfile.email || "",
      gender: userProfile.gender || "",
      ageRange: userProfile.ageRange || "",
      travelStyle: userProfile.travelStyle || "",
      aboutMe: userProfile.aboutMe || "",
    };
    setEditedProfile(sanitizedProfile);
    setIsEditing(true);
  };

  // 프로필 수정 저장 핸들러
  const handleSaveProfile = () => {
    // accessToken 가져오기
    const accessToken = localStorage.getItem("accessToken");

    if (!accessToken) {
      console.error("로그인이 필요합니다");
      router.push("/member/login");
      return;
    }

    // MemberUpdateRequestDto 형식으로 데이터 구성
    const memberUpdateRequestDto = {
      email: editedProfile.email,
      nickname: editedProfile.nickname,
      gender: editedProfile.gender,
      ageRange: editedProfile.ageRange,
      travelStyle: editedProfile.travelStyle,
      aboutMe: editedProfile.aboutMe,
      // 필요한 경우 프로필 이미지 정보 추가
    };

    // 회원정보 수정 API 호출
    fetch(`${process.env.NEXT_PUBLIC_API_URL}/member/update`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken}`,
      },
      body: JSON.stringify(memberUpdateRequestDto),
    })
      .then((response) => {
        if (!response.ok) {
          if (response.status === 401) {
            throw new Error("인증이 만료되었습니다");
          }
          throw new Error(`회원정보 수정 실패: ${response.status}`);
        }
        return response.json();
      })
      .then((responseData: RsData<MemberResponseDto>) => {
        // API 응답 처리
        if (responseData.code.startsWith("200")) {
          // 성공적으로 업데이트된 경우
          setUserProfile(responseData.data);
          setIsEditing(false);
          // 성공 메시지 표시 (옵션)
          alert(responseData.msg || "회원 정보가 수정되었습니다.");
        } else {
          // 서버에서 오류 응답을 보낸 경우
          throw new Error(responseData.msg || "회원정보 수정 실패");
        }
      })
      .catch((error) => {
        console.error("회원정보 수정 오류:", error);
        alert(error.message || "회원정보 수정 중 오류가 발생했습니다.");
      });
  };

  // 프로필 수정 취소 핸들러
  const handleCancelEdit = () => {
    setEditedProfile({ ...userProfile });
    setIsEditing(false);
  };

  // 프로필 입력 변경 핸들러
  const handleProfileChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
    >
  ) => {
    const { name, value } = e.target;
    setEditedProfile({
      ...editedProfile,
      [name]: value || "", // 값이 null이나 undefined면 빈 문자열 사용
    });
  };

  // 회원 탈퇴 모달 열기 핸들러
  const handleOpenDeleteModal = () => {
    setShowDeleteModal(true);
  };

  // 회원 탈퇴 모달 닫기 핸들러
  const handleCloseDeleteModal = () => {
    setShowDeleteModal(false);
  };

  // 회원 탈퇴 실행 핸들러
  const handleDeleteAccount = async () => {
    try {
      // accessToken 가져오기
      const accessToken = localStorage.getItem("accessToken");

      if (!accessToken) {
        console.error("로그인이 필요합니다");
        router.push("/member/login");
        return;
      }

      // 회원 탈퇴 API 호출
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/member/delete`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
          credentials: "include", // 쿠키를 포함하도록 설정
        }
      );

      if (!response.ok) {
        if (response.status === 401) {
          throw new Error("인증이 만료되었습니다");
        }
        throw new Error(`회원 탈퇴 실패: ${response.status}`);
      }

      // 회원 탈퇴 성공 처리
      let responseData;

      // 204 상태 코드인 경우 (No Content)
      if (response.status === 204) {
        responseData = { success: true, msg: "회원이 삭제되었습니다." };
      } else {
        // 응답 본문이 있는 경우 JSON으로 파싱
        try {
          responseData = await response.json();
        } catch (e) {
          // JSON 파싱 실패 시 성공으로 처리 (204이지만 본문이 없는 경우)
          responseData = { success: true, msg: "회원이 삭제되었습니다." };
        }
      }

      // 로컬 스토리지의 토큰 제거
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");

      // 추가: 로그아웃 API 호출하여 쿠키의 토큰도 제거
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
      const logoutUrl = `${apiUrl}/member/logout`;

      try {
        await fetch(logoutUrl, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "include", // 쿠키를 포함하도록 설정
        });
      } catch (logoutError) {
        console.error("로그아웃 처리 중 오류:", logoutError);
        // 로그아웃 실패해도 페이지 이동은 계속 진행
      }

      // 커스텀 이벤트 발생 (Header 컴포넌트의 상태 업데이트를 위함)
      window.dispatchEvent(new Event("logout"));

      // 성공 메시지 표시
      alert(responseData.msg || "회원 탈퇴가 완료되었습니다.");

      // 홈페이지로 리다이렉트
      router.push("/");
    } catch (error) {
      console.error("회원 탈퇴 오류:", error);
      alert(error.message || "회원 탈퇴 중 오류가 발생했습니다.");
      setShowDeleteModal(false);
    }
  };

  // 백엔드 API 호출
  useEffect(() => {
    // accessToken 가져오기
    const accessToken = localStorage.getItem("accessToken");

    // 토큰이 없을 경우 처리
    if (!accessToken) {
      console.log("로그인이 필요합니다");
      // 로그인 페이지로 리다이렉트 (Next.js의 router 사용)
      router.push("/member/login");
      return;
    }

    // 마이페이지 정보 가져오기
    fetch(`${process.env.NEXT_PUBLIC_API_URL}/member/mypage`, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    })
      .then((response) => {
        if (!response.ok) {
          if (response.status === 401) {
            console.error("인증이 만료되었습니다. 다시 로그인해 주세요.");

            // 새로운 accessToken 얻기 시도
            const refreshToken = localStorage.getItem("refreshToken");
            if (refreshToken) {
              return fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/refresh`, {
                method: "POST",
                headers: {
                  "Content-Type": "application/json",
                },
                body: JSON.stringify({ refreshToken }),
              }).then((refreshResponse) => {
                if (refreshResponse.ok) {
                  return refreshResponse.json().then((data) => {
                    // 새 accessToken 저장
                    localStorage.setItem("accessToken", data.accessToken);

                    // 새 토큰으로 다시 마이페이지 요청
                    return fetch(
                      `${process.env.NEXT_PUBLIC_API_URL}/member/mypage`,
                      {
                        headers: {
                          Authorization: `Bearer ${data.accessToken}`,
                        },
                      }
                    );
                  });
                } else {
                  // refreshToken도 만료된 경우 로그인 페이지로 리다이렉트
                  localStorage.removeItem("accessToken");
                  localStorage.removeItem("refreshToken");
                  router.push("/member/login");
                  throw new Error("로그인이 필요합니다");
                }
              });
            } else {
              // refreshToken이 없는 경우
              localStorage.removeItem("accessToken");
              router.push("/login");
              throw new Error("로그인이 필요합니다");
            }
          }
          throw new Error(`마이페이지 로딩 실패: ${response.status}`);
        }
        return response.json();
      })
      .then((responseData: RsData<MemberResponseDto>) => {
        if (responseData && responseData.data) {
          setUserProfile(responseData.data);
          setEditedProfile(responseData.data);

          // 프로필 이미지 URL 설정
          if (responseData.data.profileImage) {
            setImageUrl(getProfileImageUrl(responseData.data.profileImage));
          }

          console.log(`마이페이지 로딩 성공: ${responseData.msg}`);
        }
      })
      .catch((error) => {
        console.error("마이페이지 로딩 실패:", error);
      });
  }, []);

  // 이미지 파일 선택 핸들러
  const handleImageSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      setImageFile(file);

      // 이미지 미리보기 생성
      const reader = new FileReader();
      reader.onload = (e) => {
        if (e.target?.result) {
          setImageUrl(e.target.result.toString());
        }
      };
      reader.readAsDataURL(file);
    }
  };

  // 이미지 업로드 핸들러
  const handleImageUpload = async () => {
    if (!imageFile) return;

    setImageLoading(true);
    const accessToken = localStorage.getItem("accessToken");

    if (!accessToken) {
      alert("로그인이 필요합니다");
      setImageLoading(false);
      return;
    }

    const formData = new FormData();
    formData.append("image", imageFile);

    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/member/profile-image/upload`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
          body: formData,
        }
      );

      if (!response.ok) {
        throw new Error(`이미지 업로드 실패: ${response.status}`);
      }

      const data = await response.json();

      if (data.code.startsWith("200")) {
        // 새 이미지 URL 설정
        setUserProfile({
          ...userProfile,
          profileImage: data.data,
        });
        setImageUrl(getProfileImageUrl(data.data));
        setImageFile(null);
        alert("이미지가 업로드되었습니다.");
      } else {
        throw new Error(data.msg || "이미지 업로드 실패");
      }
    } catch (error) {
      console.error("이미지 업로드 오류:", error);
      alert(error.message || "이미지 업로드 중 오류가 발생했습니다.");
    } finally {
      setImageLoading(false);
    }
  };

  // 이미지 삭제 핸들러
  const handleImageDelete = async () => {
    const accessToken = localStorage.getItem("accessToken");

    if (!accessToken) {
      alert("로그인이 필요합니다");
      return;
    }

    if (!userProfile.profileImage) {
      alert("삭제할 이미지가 없습니다.");
      return;
    }

    if (!confirm("프로필 이미지를 삭제하시겠습니까?")) return;

    setImageLoading(true);

    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/member/profile-image/delete`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error(`이미지 삭제 실패: ${response.status}`);
      }

      const data = await response.json();

      if (data.code.startsWith("200")) {
        // 기본 이미지로 변경
        setUserProfile({
          ...userProfile,
          profileImage: null,
        });
        setImageUrl("/defaultUser.png");
        setImageFile(null);

        // 파일 입력 초기화
        if (fileInputRef.current) {
          fileInputRef.current.value = "";
        }

        alert("이미지가 삭제되었습니다.");
      } else {
        throw new Error(data.msg || "이미지 삭제 실패");
      }
    } catch (error) {
      console.error("이미지 삭제 오류:", error);
      alert(error.message || "이미지 삭제 중 오류가 발생했습니다.");
    } finally {
      setImageLoading(false);
    }
  };

  // 생성일자 포맷팅 함수
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("ko-KR");
  };

  // 여행 스타일 한글화
  const getTravelStyleInKorean = (style: string) => {
    const styles: { [key: string]: string } = {
      TOURISM: "관광",
      RELAXATION: "휴양",
      SHOPPING: "쇼핑",
      ADVENTURE: "액티비티",
      GOURMET: "미식",
    };
    return styles[style] || style;
  };

  // 연령대 한글화
  const getAgeRangeInKorean = (ageRange: string) => {
    const ranges: { [key: string]: string } = {
      TEENS: "10대",
      TWENTIES: "20대",
      THIRTIES: "30대",
      FORTIES_PLUS: "40대 이상",
    };
    return ranges[ageRange] || ageRange;
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Header />

      <div className="container mx-auto px-4 py-8">
        <h2 className="text-3xl font-bold mb-8">마이페이지</h2>

        {/* 탭 메뉴 */}
        <div className="flex border-b mb-8">
          <button
            className={`py-2 px-4 font-medium ${
              activeTab === "profile"
                ? "text-blue-600 border-b-2 border-blue-600"
                : "text-gray-500 hover:text-blue-500"
            }`}
            onClick={() => setActiveTab("profile")}
          >
            프로필
          </button>
          <button
            className={`py-2 px-4 font-medium ${
              activeTab === "myTrips"
                ? "text-blue-600 border-b-2 border-blue-600"
                : "text-gray-500 hover:text-blue-500"
            }`}
            onClick={() => setActiveTab("myTrips")}
          >
            내가 만든 여행
          </button>
          <button
            className={`py-2 px-4 font-medium ${
              activeTab === "participating"
                ? "text-blue-600 border-b-2 border-blue-600"
                : "text-gray-500 hover:text-blue-500"
            }`}
            onClick={() => setActiveTab("participating")}
          >
            참여 중인 여행
          </button>
          <button
            className={`py-2 px-4 font-medium ${
              activeTab === "completed"
                ? "text-blue-600 border-b-2 border-blue-600"
                : "text-gray-500 hover:text-blue-500"
            }`}
            onClick={() => setActiveTab("completed")}
          >
            완료된 여행
          </button>
        </div>

        {/* 프로필 섹션 */}
        {activeTab === "profile" && (
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="flex flex-col md:flex-row">
              {/* 프로필 이미지 */}
              <div className="md:w-1/3 mb-6 md:mb-0 flex flex-col items-center justify-center">
                <div className="relative w-48 h-48 mb-4">
                  <img
                    src={imageUrl}
                    alt="프로필 이미지"
                    className="w-full h-full rounded-full object-cover"
                    onError={(e) => {
                      e.currentTarget.src = "/defaultUser.png";
                    }}
                  />
                  {imageLoading && (
                    <div className="absolute inset-0 flex items-center justify-center bg-black bg-opacity-40 rounded-full">
                      <div className="animate-spin h-8 w-8 border-4 border-blue-500 rounded-full border-t-transparent"></div>
                    </div>
                  )}
                </div>
                {isEditing && (
                  <div className="space-y-2 w-full max-w-xs">
                    <input
                      type="file"
                      accept="image/*"
                      onChange={handleImageSelect}
                      className="hidden"
                      ref={fileInputRef}
                      id="profile-image-input"
                    />

                    <div className="flex space-x-2">
                      <label
                        htmlFor="profile-image-input"
                        className="bg-gray-200 text-gray-800 px-3 py-1 rounded-md hover:bg-gray-300 cursor-pointer text-sm flex-1 text-center"
                      >
                        이미지 선택
                      </label>

                      {imageFile && (
                        <button
                          onClick={handleImageUpload}
                          className="bg-blue-600 text-white px-3 py-1 rounded-md hover:bg-blue-700 text-sm flex-1"
                          disabled={imageLoading}
                        >
                          업로드
                        </button>
                      )}
                    </div>

                    {userProfile.profileImage && (
                      <button
                        onClick={handleImageDelete}
                        className="bg-red-600 text-white px-3 py-1 rounded-md hover:bg-red-700 text-sm w-full"
                        disabled={imageLoading}
                      >
                        이미지 삭제
                      </button>
                    )}
                  </div>
                )}
                {!isEditing && (
                  <div className="space-y-2 w-full max-w-xs">
                    <button
                      onClick={handleEditProfile}
                      className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 w-full"
                    >
                      프로필 수정
                    </button>
                    <button
                      onClick={handleOpenDeleteModal}
                      className="bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700 w-full"
                    >
                      회원 탈퇴
                    </button>
                  </div>
                )}
              </div>

              {/* 프로필 정보 */}
              <div className="md:w-2/3 md:pl-8">
                {isEditing ? (
                  // 수정 모드
                  <div>
                    <div className="mb-4">
                      <label className="block text-gray-700 mb-2">닉네임</label>
                      <input
                        type="text"
                        name="nickname"
                        value={editedProfile.nickname}
                        onChange={handleProfileChange}
                        className="w-full p-2 border rounded-md"
                      />
                    </div>
                    <div className="mb-4">
                      <label className="block text-gray-700 mb-2">이메일</label>
                      <input
                        type="email"
                        name="email"
                        value={editedProfile.email}
                        onChange={handleProfileChange}
                        className="w-full p-2 border rounded-md"
                      />
                    </div>
                    <div className="mb-4">
                      <label className="block text-gray-700 mb-2">성별</label>
                      <select
                        name="gender"
                        value={editedProfile.gender}
                        onChange={handleProfileChange}
                        className="w-full p-2 border rounded-md"
                      >
                        <option value="MALE">남성</option>
                        <option value="FEMALE">여성</option>
                      </select>
                    </div>
                    <div className="mb-4">
                      <label className="block text-gray-700 mb-2">연령대</label>
                      <select
                        name="ageRange"
                        value={editedProfile.ageRange}
                        onChange={handleProfileChange}
                        className="w-full p-2 border rounded-md"
                      >
                        <option value="TEENS">10대</option>
                        <option value="TWENTIES">20대</option>
                        <option value="THIRTIES">30대</option>
                        <option value="FORTIES_PLUS">40대 이상</option>
                      </select>
                    </div>
                    <div className="mb-4">
                      <label className="block text-gray-700 mb-2">
                        여행 스타일
                      </label>
                      <select
                        name="travelStyle"
                        value={editedProfile.travelStyle}
                        onChange={handleProfileChange}
                        className="w-full p-2 border rounded-md"
                      >
                        <option value="TOURISM">관광</option>
                        <option value="RELAXATION">휴양</option>
                        <option value="SHOPPING">쇼핑</option>
                        <option value="ADVENTURE">액티비티</option>
                        <option value="GOURMET">미식</option>
                      </select>
                    </div>
                    <div className="mb-4">
                      <label className="block text-gray-700 mb-2">
                        자기소개
                      </label>
                      <textarea
                        name="aboutMe"
                        value={editedProfile.aboutMe}
                        onChange={handleProfileChange}
                        className="w-full p-2 border rounded-md h-32"
                      ></textarea>
                    </div>
                    <div className="flex space-x-2">
                      <button
                        onClick={handleSaveProfile}
                        className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
                      >
                        저장하기
                      </button>
                      <button
                        onClick={handleCancelEdit}
                        className="bg-gray-300 text-gray-800 px-4 py-2 rounded-md hover:bg-gray-400"
                      >
                        취소
                      </button>
                    </div>
                  </div>
                ) : (
                  // 표시 모드
                  <div>
                    <h3 className="text-2xl font-bold mb-2">
                      {userProfile.nickname}
                    </h3>
                    <p className="text-gray-600 mb-1">{userProfile.email}</p>
                    <p className="text-gray-600 mb-4">
                      평점: ⭐ {userProfile.rating?.toFixed(1) || "평가 없음"}
                    </p>
                    <div className="mb-4">
                      <h4 className="text-lg font-semibold mb-2">기본 정보</h4>
                      <p className="text-gray-700 mb-1">
                        성별:{" "}
                        {userProfile.gender === "MALE"
                          ? "남성"
                          : userProfile.gender === "FEMALE"
                          ? "여성"
                          : "기타"}
                      </p>
                      <p className="text-gray-700 mb-1">
                        연령대: {getAgeRangeInKorean(userProfile.ageRange)}
                      </p>
                      <p className="text-gray-700 mb-1">
                        여행 스타일:{" "}
                        {getTravelStyleInKorean(userProfile.travelStyle)}
                      </p>
                    </div>
                    <div className="mb-6">
                      <h4 className="text-lg font-semibold mb-2">자기소개</h4>
                      <p className="text-gray-700">{userProfile.aboutMe}</p>
                    </div>
                    <div>
                      <h4 className="text-lg font-semibold mb-2">회원 정보</h4>
                      <p className="text-gray-700">
                        가입일: {formatDate(userProfile.createdAt)}
                      </p>
                      <p className="text-gray-700">
                        작성한 여행: {myTrips.length}개
                      </p>
                      <p className="text-gray-700">
                        참여 중인 여행: {participatingTrips.length}개
                      </p>
                      <p className="text-gray-700">
                        완료한 여행: {completedTrips.length}개
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {/* 내가 만든 여행 섹션 */}
        {activeTab === "myTrips" && (
          <div>
            <Schedule />
          </div>
        )}

        {/* 참여 중인 여행 섹션 */}
        {activeTab === "participating" && (
          <div>
            <h3 className="text-xl font-bold mb-4">참여 중인 여행</h3>

            {participatingTrips.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {participatingTrips.map((trip) => (
                  <div
                    key={trip.id}
                    className="bg-white rounded-lg shadow-md p-6"
                  >
                    <h4 className="text-lg font-semibold mb-2">{trip.title}</h4>
                    <div className="mb-4">
                      <p className="text-gray-600 mb-1">
                        여행지: {trip.destination}
                      </p>
                      <p className="text-gray-600 mb-1">날짜: {trip.date}</p>
                      <p className="text-gray-600 mb-1">
                        상태:{" "}
                        <span
                          className={`font-medium ${
                            trip.status === "준비중"
                              ? "text-blue-600"
                              : trip.status === "여행중"
                              ? "text-green-600"
                              : "text-gray-600"
                          }`}
                        >
                          {trip.status}
                        </span>
                      </p>
                      <p className="text-gray-600">
                        참여인원: {trip.companions}명
                      </p>
                    </div>
                    <Link
                      href={`/trip/${trip.id}`}
                      className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 inline-block"
                    >
                      상세 보기
                    </Link>
                  </div>
                ))}
              </div>
            ) : (
              <div className="bg-white rounded-lg shadow-md p-6 text-center">
                <p className="text-gray-500 mb-4">참여 중인 여행이 없습니다.</p>
                <Link
                  href="/trips"
                  className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
                >
                  여행 찾아보기
                </Link>
              </div>
            )}
          </div>
        )}

        {/* 완료된 여행 섹션 */}
        {activeTab === "completed" && (
          <div>
            <h3 className="text-xl font-bold mb-4">완료된 여행</h3>

            {completedTrips.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {completedTrips.map((trip) => (
                  <div
                    key={trip.id}
                    className="bg-white rounded-lg shadow-md p-6"
                  >
                    <h4 className="text-lg font-semibold mb-2">{trip.title}</h4>
                    <div className="mb-4">
                      <p className="text-gray-600 mb-1">
                        여행지: {trip.destination}
                      </p>
                      <p className="text-gray-600 mb-1">날짜: {trip.date}</p>
                      <p className="text-gray-600">
                        참여인원: {trip.companions}명
                      </p>
                    </div>
                    <div className="flex space-x-2">
                      <Link
                        href={`/trip/${trip.id}`}
                        className="bg-blue-600 text-white px-3 py-1 rounded-md hover:bg-blue-700 text-sm"
                      >
                        상세 보기
                      </Link>
                      <Link
                        href={`/trip/review/${trip.id}`}
                        className="bg-gray-200 text-gray-800 px-3 py-1 rounded-md hover:bg-gray-300 text-sm"
                      >
                        후기 작성
                      </Link>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="bg-white rounded-lg shadow-md p-6 text-center">
                <p className="text-gray-500">아직 완료된 여행이 없습니다.</p>
              </div>
            )}
          </div>
        )}
      </div>

      {/* 회원 탈퇴 확인 모달 */}
      {showDeleteModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-md w-full">
            <h3 className="text-xl font-bold mb-4">회원 탈퇴</h3>
            <p className="text-gray-700 mb-6">
              정말로 탈퇴하시겠습니까? 모든 회원 정보와 여행 데이터가 삭제되며
              이 작업은 되돌릴 수 없습니다.
            </p>
            <div className="flex justify-end space-x-2">
              <button
                onClick={handleCloseDeleteModal}
                className="px-4 py-2 bg-gray-300 rounded-md hover:bg-gray-400"
              >
                취소
              </button>
              <button
                onClick={handleDeleteAccount}
                className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
              >
                탈퇴하기
              </button>
            </div>
          </div>
        </div>
      )}

      <Footer />
    </div>
  );
}
