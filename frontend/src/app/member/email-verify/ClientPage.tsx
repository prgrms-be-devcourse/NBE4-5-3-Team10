"use client";
import { useState, useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import axios from "axios";
import Header from "@/components/Header";
import Footer from "@/components/Footer";

export default function ClientPage() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const emailFromParams = searchParams.get("email");
  const nameFromParams = searchParams.get("name");

  const [formData, setFormData] = useState({
    email: emailFromParams || "",
    authCode: "",
  });
  const [errors, setErrors] = useState({
    email: "",
    authCode: "",
  });
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    if (emailFromParams) {
      const timer = setTimeout(() => {
        handleSendCode();
      }, 500);

      return () => clearTimeout(timer);
    }
  }, [emailFromParams]);

  const validateAuthCode = () => {
    const newErrors = {
      ...errors,
      authCode: "",
    };
    let isValid = true;

    if (!formData.authCode) {
      newErrors.authCode = "인증 코드는 필수 입력값입니다.";
      isValid = false;
    } else if (formData.authCode.length !== 6) {
      newErrors.authCode = "인증 코드는 6자리여야 합니다.";
      isValid = false;
    }

    setErrors(newErrors);
    return isValid;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleVerifyCode = async (e) => {
    e.preventDefault();

    if (!validateAuthCode()) return;

    setIsLoading(true);
    setMessage(null);

    try {
      const backendUrl =
        process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

      const response = await axios.post(`${backendUrl}/member/auth/email`, {
        email: formData.email,
        authCode: formData.authCode,
      });

      setMessage({
        text: "이메일 인증에 성공하였습니다. 로그인 페이지로 이동합니다.",
        isError: false,
      });

      setTimeout(() => {
        router.push("/member/login");
      }, 3000);
    } catch (error) {
      const errorMessage =
        error.response?.data || "이메일 인증에 실패하였습니다.";
      setMessage({
        text:
          typeof errorMessage === "string"
            ? errorMessage
            : "이메일 인증에 실패하였습니다.",
        isError: true,
      });
      setIsLoading(false);
    }
  };

  const handleSendCode = async () => {
    if (!formData.email) {
      setErrors({
        ...errors,
        email: "이메일은 필수 입력값입니다.",
      });
      return;
    }

    try {
      setIsLoading(true);
      const backendUrl =
        process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

      const encodedEmail = encodeURIComponent(formData.email);
      const response = await axios.get(
        `${backendUrl}/member/auth/verify-email?email=${encodedEmail}`
      );

      console.log("Send code response:", response.data);

      setMessage({
        text: "인증 코드가 발송되었습니다.",
        isError: false,
      });
    } catch (error) {
      console.error("Email verification code request error:", error);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "인증 코드 발송에 실패하였습니다.";
      setMessage({
        text:
          typeof errorMessage === "string"
            ? errorMessage
            : "인증 코드 발송에 실패하였습니다.",
        isError: true,
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 헤더 컴포넌트 사용 */}
      <Header />

      {/* 이메일 인증 섹션 */}
      <div className="bg-blue-600 text-white">
        <div className="container mx-auto px-4 py-12 md:py-16">
          <div className="max-w-2xl">
            <h2 className="text-3xl md:text-4xl font-bold mb-4">이메일 인증</h2>
            <p className="text-xl mb-6">
              안전한 서비스 이용을 위해 이메일 인증을 진행해주세요.
            </p>
          </div>
        </div>
      </div>

      {/* 단계 표시 - 2단계로 변경 */}
      <div className="container mx-auto px-4 pt-12">
        <div className="max-w-lg mx-auto">
          <div className="mb-8">
            <div className="flex items-center justify-between">
              <div className="flex flex-col items-center">
                <div className="w-10 h-10 rounded-full flex items-center justify-center bg-blue-600 text-white">
                  1
                </div>
                <span className="text-sm mt-2">정보 입력</span>
              </div>
              <div className="flex-1 h-1 mx-2 bg-blue-600"></div>
              <div className="flex flex-col items-center">
                <div className="w-10 h-10 rounded-full flex items-center justify-center bg-blue-600 text-white">
                  2
                </div>
                <span className="text-sm mt-2">이메일 인증</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 인증 폼 섹션 */}
      <div className="container mx-auto px-4 py-6">
        <div className="max-w-lg mx-auto bg-white rounded-lg shadow-md p-8">
          <p className="text-lg text-gray-700 mb-6">
            {nameFromParams ? `${nameFromParams}님, ` : ""}인증 코드가 이메일로
            발송되었습니다. 받으신 코드를 아래에 입력해주세요.
          </p>

          <form onSubmit={handleVerifyCode}>
            <div className="mb-6">
              <label
                htmlFor="email"
                className="block text-gray-700 text-lg font-medium mb-2"
              >
                이메일
              </label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                className="w-full p-3 bg-gray-100 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                readOnly
              />
            </div>

            <div className="mb-6">
              <label
                htmlFor="authCode"
                className="block text-gray-700 text-lg font-medium mb-2"
              >
                인증 코드
              </label>
              <input
                type="text"
                id="authCode"
                name="authCode"
                placeholder="6자리 인증 코드"
                maxLength={6}
                value={formData.authCode}
                onChange={handleChange}
                className={`w-full p-3 bg-gray-100 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                  errors.authCode ? "border-red-500" : "border-gray-300"
                }`}
              />
              {errors.authCode && (
                <p className="mt-1 text-red-500 text-sm">{errors.authCode}</p>
              )}
            </div>

            {message && (
              <div
                className={`p-4 mb-6 rounded-md ${
                  message.isError
                    ? "bg-red-100 text-red-700"
                    : "bg-green-100 text-green-700"
                }`}
              >
                {message.text}
              </div>
            )}

            <button
              type="submit"
              disabled={isLoading}
              className="w-full bg-blue-600 text-white py-3 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors disabled:opacity-70"
            >
              {isLoading ? "처리 중..." : "인증 완료"}
            </button>
          </form>

          <div className="mt-6 pt-6 border-t border-gray-200 text-center">
            <p className="text-gray-600">
              인증 코드를 받지 못하셨나요?{" "}
              <button
                onClick={handleSendCode}
                disabled={isLoading}
                className="text-blue-600 hover:underline focus:outline-none disabled:opacity-70"
              >
                인증 코드 재발송
              </button>
            </p>
          </div>
        </div>
      </div>

      {/* 안내 섹션 */}
      <div className="container mx-auto px-4 py-12">
        <div className="max-w-4xl mx-auto">
          <h3 className="text-2xl font-bold text-center mb-8">
            이메일 인증 안내
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
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
                    d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                  />
                </svg>
              </div>
              <h4 className="text-xl font-semibold mb-2">정보 입력</h4>
              <p className="text-gray-600">
                회원 정보와 이메일을 입력하여 가입을 시작합니다.
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
                    d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
              </div>
              <h4 className="text-xl font-semibold mb-2">이메일 인증</h4>
              <p className="text-gray-600">
                이메일로 받은 인증 코드를 입력하여 가입을 완료합니다.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* 푸터 컴포넌트 사용 */}
      <Footer />
    </div>
  );
}
