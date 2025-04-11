"use client";

import type React from "react";

import { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import Header from "@/components/Header";
import Footer from "@/components/Footer";

// 폼 데이터 타입 정의
interface FormData {
  username: string;
  password: string;
  rememberMe: boolean;
}

// 에러 타입 정의
interface FormErrors {
  username: string;
  password: string;
  general: string;
}

// 인증 응답 타입 정의
interface AuthResponse {
  accessToken: string;
  refreshToken?: string;
  message?: string;
  isDeletedAccount?: boolean;
  authority?: string;
}

// RsData 응답 타입 정의
interface RsData<T> {
  resultCode: string;
  code: string;
  msg: string;
  data: T;
}

export default function ClientPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [formData, setFormData] = useState<FormData>({
    username: "",
    password: "",
    rememberMe: false,
  });
  const [errors, setErrors] = useState<FormErrors>({
    username: "",
    password: "",
    general: "",
  });
  const [isLoading, setIsLoading] = useState(false);
  const [showRegisteredMessage, setShowRegisteredMessage] = useState(false);
  const [showRestoreModal, setShowRestoreModal] = useState(false);
  const [restoringAccount, setRestoringAccount] = useState(false);

  useEffect(() => {
    // Check if user just registered
    const registered = searchParams.get("registered");
    if (registered) {
      setShowRegisteredMessage(true);

      // Clear the message after 5 seconds
      const timer = setTimeout(() => {
        setShowRegisteredMessage(false);
      }, 5000);

      return () => clearTimeout(timer);
    }
  }, [searchParams]);

  // 소셜 로그인 처리를 위한 useEffect 추가
  useEffect(() => {
    // 소셜 로그인 완료 후 토큰 확인
    const accessToken = searchParams.get("accessToken");
    const refreshToken = searchParams.get("refreshToken");
    const error = searchParams.get("error");

    if (accessToken) {
      console.log("소셜 로그인 성공, 액세스 토큰 수신:", accessToken);

      // 액세스 토큰 저장
      localStorage.setItem("accessToken", accessToken);

      // 리프레시 토큰이 있으면 저장
      if (refreshToken) {
        console.log("리프레시 토큰 수신:", refreshToken);
        localStorage.setItem("refreshToken", refreshToken);
      }

      // 사용자 정의 이벤트 발생
      window.dispatchEvent(new Event("login"));

      // 토큰 파라미터 제거 (URL 정리)
      const url = new URL(window.location.href);
      const params = new URLSearchParams(url.search);
      params.delete("accessToken");
      params.delete("refreshToken");
      const newUrl =
        url.pathname + (params.toString() ? "?" + params.toString() : "");
      window.history.replaceState({}, document.title, newUrl);

      // 홈페이지로 리디렉션
      router.push("/");
    } else if (error) {
      // 소셜 로그인 에러 처리
      setErrors({
        ...errors,
        general: "소셜 로그인에 실패했습니다: " + error,
      });
    }
  }, [searchParams, router, errors]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === "checkbox" ? checked : value,
    });

    // Clear the error when user starts typing
    if (name in errors) {
      setErrors({
        ...errors,
        [name as keyof FormErrors]: "",
      });
    }
  };

  const validateForm = () => {
    let isValid = true;
    const newErrors = { ...errors };

    // Username validation
    if (!formData.username) {
      newErrors.username = "아이디는 필수 입력값입니다";
      isValid = false;
    }

    // Password validation
    if (!formData.password) {
      newErrors.password = "비밀번호는 필수 입력값입니다";
      isValid = false;
    }

    setErrors(newErrors);
    return isValid;
  };

  const handleRestoreAccount = async () => {
    setRestoringAccount(true);
    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
      const restoreUrl = `${apiUrl}/member/restore`;

      // 액세스 토큰 가져오기 (로컬 스토리지 또는 쿠키에서)
      const accessToken = localStorage.getItem("accessToken");

      const response = await fetch(restoreUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
        credentials: "include", // Include cookies
      });

      const responseText = await response.text();
      let responseData;

      try {
        responseData = JSON.parse(responseText);
      } catch (e) {
        throw new Error("서버 응답을 처리할 수 없습니다.");
      }

      if (
        !response.ok ||
        !responseData.code ||
        !responseData.code.startsWith("200")
      ) {
        throw new Error(responseData.msg || "계정 복구에 실패했습니다.");
      }

      // 복구 성공 후 로컬 스토리지에서 토큰 제거
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");

      // 성공 메시지를 표시하기 위한 상태 설정
      setErrors({
        ...errors,
        general: "계정이 성공적으로 복구되었습니다. 다시 로그인해 주세요.",
      });

      // 모달 닫기
      setShowRestoreModal(false);

      // 로그인 페이지에 머무름 (리디렉션 없음)
      // 토큰을 제거했으므로 사용자는 다시 로그인해야 합니다
    } catch (error) {
      console.error("계정 복구 오류:", error);
      setErrors({
        ...errors,
        general:
          error instanceof Error
            ? error.message
            : "계정 복구 처리 중 오류가 발생했습니다.",
      });
    } finally {
      setRestoringAccount(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);

    try {
      // 절대 경로로 API 엔드포인트 지정 (개발 서버 포트에 맞게 수정)
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
      const loginUrl = `${apiUrl}/member/login`;

      console.log("로그인 요청 URL:", loginUrl);
      console.log("요청 데이터:", {
        username: formData.username,
        password: formData.password,
      });

      // Make API call to Spring backend login endpoint
      const response = await fetch(loginUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: JSON.stringify({
          username: formData.username,
          password: formData.password,
        }),
        credentials: "include", // Important to include cookies
      });

      console.log("응답 상태:", response.status);
      console.log("응답 헤더:", Object.fromEntries([...response.headers]));

      // 응답 내용을 텍스트로 먼저 확인
      const responseText = await response.text();
      console.log("응답 텍스트:", responseText);

      // 응답이 비어있거나 HTML인 경우 처리
      if (!responseText || responseText.trim().startsWith("<!DOCTYPE")) {
        throw new Error("서버에서 유효하지 않은 응답을 반환했습니다.");
      }

      // JSON 파싱 부분을 RsData 구조에 맞게 수정
      let rsData;
      try {
        rsData = JSON.parse(responseText);
      } catch (e) {
        console.error("JSON 파싱 오류:", e);
        throw new Error("서버 응답을 처리할 수 없습니다.");
      }

      // 응답 검증
      // code 필드가 "200-"으로 시작하는지 확인
      if (!response.ok || !rsData.code || !rsData.code.startsWith("200")) {
        throw new Error(rsData.msg || "로그인에 실패했습니다.");
      }

      console.log("인증 응답 데이터:", rsData);

      // authData는 이제 rsData.data에 있음
      const authData = rsData.data;

      if (!authData || !authData.accessToken) {
        throw new Error("인증 정보가 없습니다.");
      }

      // 소프트 딜리트된 계정인 경우 복구 모달 표시 - isDeletedAccount 체크로 수정
      if (authData.isDeletedAccount) {
        console.log("삭제된 계정으로 로그인 시도:", authData.isDeletedAccount);

        // 토큰 저장
        localStorage.setItem("accessToken", authData.accessToken);
        if (authData.refreshToken) {
          localStorage.setItem("refreshToken", authData.refreshToken);
        }

        // 복구 모달 표시
        setShowRestoreModal(true);
        setIsLoading(false);
        return;
      }

      // 정상 계정인 경우 일반 로그인 처리
      localStorage.setItem("accessToken", authData.accessToken);

      // 사용자 권한 정보 저장
      if (authData.authority) {
        localStorage.setItem("userAuthority", authData.authority);
      }

      // 사용자 정의 이벤트 발생
      window.dispatchEvent(new Event("login"));

      if (formData.rememberMe && authData.refreshToken) {
        localStorage.setItem("refreshToken", authData.refreshToken);
      }

      // 쿠키 확인 (HttpOnly 쿠키는 보이지 않음)
      console.log("현재 쿠키:", document.cookie);

      // 권한이 admin인 사용자는 관리자 페이지로, 그 외는 홈페이지로 리디렉션
      if (authData.authority === "ADMIN") {
        console.log("관리자 권한 확인: 관리자 페이지로 이동");
        router.push("/admin");
      } else {
        console.log("일반 사용자 권한: 홈페이지로 이동");
        router.push("/");
      }
    } catch (error: unknown) {
      console.error("로그인 오류:", error);
      setErrors({
        ...errors,
        general:
          error instanceof Error
            ? error.message
            : "로그인 처리 중 오류가 발생했습니다.",
      });
    } finally {
      setIsLoading(false);
    }
  };

  // 소셜 로그인 함수 업데이트 - 리디렉션 URL 지정
  const handleSocialLogin = (provider: string) => {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
    // 현재 URL을 기준으로 리디렉션 URL 설정 (로그인 페이지로 돌아옴)
    const redirectUri = encodeURIComponent(
      window.location.origin + window.location.pathname
    );

    console.log(`${provider} 소셜 로그인 시도, 리디렉션 URL: ${redirectUri}`);

    // 리디렉션 URL 파라미터를 추가하여 소셜 로그인 페이지로 이동
    window.location.href = `${apiUrl}/oauth2/authorization/${provider}?redirect_uri=${redirectUri}`;
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <div className="container mx-auto px-4 py-12">
        <div className="max-w-md mx-auto bg-white rounded-lg shadow-md p-8">
          <h2 className="text-2xl font-bold text-center text-gray-800 mb-6">
            TripFriend 로그인
          </h2>
          {showRegisteredMessage && (
            <div className="mb-6 bg-green-100 text-green-700 p-3 rounded-lg">
              회원가입이 완료되었습니다. 이제 로그인할 수 있습니다.
            </div>
          )}
          {errors.general && (
            <div className="mb-6 bg-red-100 text-red-700 p-3 rounded-lg">
              {errors.general}
            </div>
          )}
          <form onSubmit={handleSubmit}>
            <div className="mb-4">
              <label
                htmlFor="username"
                className="block text-gray-700 font-medium mb-2"
              >
                아이디
              </label>
              <input
                type="text"
                id="username"
                name="username"
                placeholder="사용자 아이디"
                className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
                  errors.username
                    ? "border-red-500 focus:ring-red-200"
                    : "border-gray-300 focus:ring-blue-200"
                }`}
                value={formData.username}
                onChange={handleChange}
              />
              {errors.username && (
                <p className="text-red-500 text-sm mt-1">{errors.username}</p>
              )}
            </div>

            <div className="mb-6">
              <div className="flex justify-between items-center mb-2">
                <label
                  htmlFor="password"
                  className="block text-gray-700 font-medium"
                >
                  비밀번호
                </label>
                <Link
                  href="/member/forgot-password"
                  className="text-sm text-blue-600 hover:underline"
                >
                  비밀번호 찾기
                </Link>
              </div>
              <input
                type="password"
                id="password"
                name="password"
                placeholder="비밀번호"
                className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
                  errors.password
                    ? "border-red-500 focus:ring-red-200"
                    : "border-gray-300 focus:ring-blue-200"
                }`}
                value={formData.password}
                onChange={handleChange}
              />
              {errors.password && (
                <p className="text-red-500 text-sm mt-1">{errors.password}</p>
              )}
            </div>

            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center">
                <input
                  type="checkbox"
                  id="rememberMe"
                  name="rememberMe"
                  className="mr-2"
                  checked={formData.rememberMe}
                  onChange={handleChange}
                />
                <label htmlFor="rememberMe" className="text-gray-700">
                  로그인 상태 유지
                </label>
              </div>
            </div>

            <button
              type="submit"
              className={`w-full bg-blue-600 text-white py-2 rounded-lg font-medium transition duration-300 ${
                isLoading
                  ? "opacity-70 cursor-not-allowed"
                  : "hover:bg-blue-700"
              }`}
              disabled={isLoading}
            >
              {isLoading ? "로그인 중..." : "로그인"}
            </button>
          </form>
          <div className="mt-6 text-center">
            <p className="text-gray-600">
              계정이 없으신가요?{" "}
              <Link
                href="/member/signup"
                className="text-blue-600 hover:underline"
              >
                회원가입
              </Link>
            </p>
          </div>
          <div className="mt-8 pt-6 border-t border-gray-200">
            <p className="text-center text-gray-600 mb-4">
              또는 소셜 계정으로 로그인
            </p>
            <div className="flex flex-col space-y-3">
              <button
                type="button"
                onClick={() => handleSocialLogin("google")}
                className="flex items-center justify-center w-full px-4 py-2 bg-gray-100 rounded-lg hover:bg-gray-200 transition duration-300"
              >
                <svg className="w-5 h-5 mr-2" viewBox="0 0 24 24">
                  <path
                    fill="#4285F4"
                    d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                  />
                  <path
                    fill="#34A853"
                    d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                  />
                  <path
                    fill="#FBBC05"
                    d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
                  />
                  <path
                    fill="#EA4335"
                    d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                  />
                </svg>
                Google로 로그인
              </button>

              <button
                type="button"
                onClick={() => handleSocialLogin("kakao")}
                className="flex items-center justify-center w-full px-4 py-2 bg-yellow-300 rounded-lg hover:bg-yellow-400 transition duration-300 text-gray-800"
              >
                <svg
                  className="w-5 h-5 mr-2"
                  viewBox="0 0 24 24"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    d="M12 3C6.477 3 2 6.477 2 10.8C2 13.7 3.859 16.261 6.686 17.635C6.458 18.498 5.506 21.094 5.444 21.376C5.371 21.724 5.651 21.99 5.957 21.794C6.206 21.639 9.334 19.499 10.476 18.72C10.981 18.802 11.481 18.843 12 18.843C17.523 18.843 22 15.366 22 10.8C22 6.477 17.523 3 12 3Z"
                    fill="#371D1E"
                  />
                </svg>
                카카오로 로그인
              </button>

              <button
                type="button"
                onClick={() => handleSocialLogin("naver")}
                className="flex items-center justify-center w-full px-4 py-2 bg-green-500 rounded-lg hover:bg-green-600 transition duration-300 text-white"
              >
                <svg
                  className="w-5 h-5 mr-2"
                  viewBox="0 0 24 24"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    d="M16.273 12.845L7.376 0H0V24H7.727V11.155L16.624 24H24V0H16.273V12.845Z"
                    fill="white"
                  />
                </svg>
                네이버로 로그인
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* 계정 복구 모달 */}
      {showRestoreModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h3 className="text-xl font-bold mb-4">계정 복구</h3>
            <p className="text-gray-700 mb-6">
              해당 계정은 이전에 삭제 요청되었습니다. 계정을 복구하시겠습니까?
            </p>
            <div className="flex justify-end gap-3">
              <button
                onClick={() => setShowRestoreModal(false)}
                className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-100"
                disabled={restoringAccount}
              >
                취소
              </button>
              <button
                onClick={handleRestoreAccount}
                className={`px-4 py-2 bg-blue-600 text-white rounded-lg ${
                  restoringAccount
                    ? "opacity-70 cursor-not-allowed"
                    : "hover:bg-blue-700"
                }`}
                disabled={restoringAccount}
              >
                {restoringAccount ? "처리 중..." : "계정 복구하기"}
              </button>
            </div>
          </div>
        </div>
      )}

      <Footer />
    </div>
  );
}
