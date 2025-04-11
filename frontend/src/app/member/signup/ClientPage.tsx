"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import axios from "axios";

export default function ClientPage() {
  const router = useRouter();

  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    nickname: "",
    gender: "MALE",
    ageRange: "TWENTIES",
    travelStyle: "TOURISM",
    aboutMe: "",
  });

  const [errors, setErrors] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    nickname: "",
    aboutMe: "",
  });

  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState(null);

  const [step, setStep] = useState(1);

  const validateForm = () => {
    const newErrors = {
      username: "",
      email: "",
      password: "",
      confirmPassword: "",
      nickname: "",
      aboutMe: "",
    };
    let isValid = true;

    if (!formData.username) {
      newErrors.username = "사용자명은 필수 입력값입니다.";
      isValid = false;
    } else if (formData.username.length < 4) {
      newErrors.username = "사용자명은 최소 4자 이상이어야 합니다.";
      isValid = false;
    }

    if (!formData.email) {
      newErrors.email = "이메일은 필수 입력값입니다.";
      isValid = false;
    } else if (!/^\S+@\S+\.\S+$/.test(formData.email)) {
      newErrors.email = "올바른 이메일 형식을 입력해주세요.";
      isValid = false;
    }

    if (!formData.password) {
      newErrors.password = "비밀번호는 필수 입력값입니다.";
      isValid = false;
    } else if (formData.password.length < 8) {
      newErrors.password = "비밀번호는 최소 8자 이상이어야 합니다.";
      isValid = false;
    }

    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = "비밀번호가 일치하지 않습니다.";
      isValid = false;
    }

    if (!formData.nickname) {
      newErrors.nickname = "닉네임은 필수 입력값입니다.";
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

  const handleInfoSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) return;

    setIsLoading(true);
    setMessage(null);

    try {
      const backendUrl =
        process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

      const response = await axios.post(`${backendUrl}/member/join`, {
        username: formData.username,
        email: formData.email,
        password: formData.password,
        nickname: formData.nickname,
        gender: formData.gender,
        ageRange: formData.ageRange,
        travelStyle: formData.travelStyle,
        aboutMe: formData.aboutMe || "",
      });

      console.log("Registration response:", response.data);

      const queryParams = new URLSearchParams({
        email: formData.email,
        name: formData.nickname,
        username: formData.username,
        password: formData.password,
        nickname: formData.nickname,
        gender: formData.gender,
        ageRange: formData.ageRange,
        travelStyle: formData.travelStyle,
        aboutMe: formData.aboutMe || "",
      });

      router.push(`/member/email-verify?${queryParams.toString()}`);
    } catch (error) {
      console.error("Registration error:", error);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "회원가입에 실패하였습니다.";
      setMessage({
        text: errorMessage,
        isError: true,
      });
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      {/* 회원가입 헤더 섹션 */}
      <div className="bg-blue-600 text-white">
        <div className="container mx-auto px-4 py-12 md:py-16">
          <div className="max-w-2xl">
            <h2 className="text-3xl md:text-4xl font-bold mb-4">회원가입</h2>
            <p className="text-xl mb-6">
              여행 메이트와 함께 새로운 여행을 시작해보세요.
            </p>
          </div>
        </div>
      </div>

      {/* 회원가입 폼 섹션 */}
      <div className="container mx-auto px-4 py-12">
        <div className="max-w-2xl mx-auto bg-white rounded-lg shadow-md p-8">
          {/* 단계 표시 - 2단계로 변경 */}
          <div className="mb-8">
            <div className="flex items-center justify-between">
              <div className="flex flex-col items-center">
                <div
                  className={`w-10 h-10 rounded-full flex items-center justify-center ${
                    step >= 1
                      ? "bg-blue-600 text-white"
                      : "bg-gray-200 text-gray-600"
                  }`}
                >
                  1
                </div>
                <span className="text-sm mt-2">정보 입력</span>
              </div>
              <div
                className={`flex-1 h-1 mx-2 ${
                  step >= 2 ? "bg-blue-600" : "bg-gray-200"
                }`}
              ></div>
              <div className="flex flex-col items-center">
                <div
                  className={`w-10 h-10 rounded-full flex items-center justify-center ${
                    step >= 2
                      ? "bg-blue-600 text-white"
                      : "bg-gray-200 text-gray-600"
                  }`}
                >
                  2
                </div>
                <span className="text-sm mt-2">이메일 인증</span>
              </div>
            </div>
          </div>

          {/* 정보 입력 폼 (1단계) */}
          <form onSubmit={handleInfoSubmit}>
            <div>
              <div className="mb-6">
                <label
                  htmlFor="username"
                  className="block text-gray-700 text-lg font-medium mb-2"
                >
                  아이디
                </label>
                <input
                  type="text"
                  id="username"
                  name="username"
                  placeholder="4자 이상의 아이디"
                  value={formData.username}
                  onChange={handleChange}
                  className={`w-full p-3 bg-gray-100 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                    errors.username ? "border-red-500" : "border-gray-300"
                  }`}
                />
                {errors.username && (
                  <p className="mt-1 text-red-500 text-sm">{errors.username}</p>
                )}
              </div>

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
                  placeholder="example@email.com"
                  value={formData.email}
                  onChange={handleChange}
                  className={`w-full p-3 bg-gray-100 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                    errors.email ? "border-red-500" : "border-gray-300"
                  }`}
                />
                {errors.email && (
                  <p className="mt-1 text-red-500 text-sm">{errors.email}</p>
                )}
              </div>

              <div className="mb-6">
                <label
                  htmlFor="password"
                  className="block text-gray-700 text-lg font-medium mb-2"
                >
                  비밀번호
                </label>
                <input
                  type="password"
                  id="password"
                  name="password"
                  placeholder="8자 이상의 비밀번호"
                  value={formData.password}
                  onChange={handleChange}
                  className={`w-full p-3 bg-gray-100 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                    errors.password ? "border-red-500" : "border-gray-300"
                  }`}
                />
                {errors.password && (
                  <p className="mt-1 text-red-500 text-sm">{errors.password}</p>
                )}
              </div>

              <div className="mb-6">
                <label
                  htmlFor="confirmPassword"
                  className="block text-gray-700 text-lg font-medium mb-2"
                >
                  비밀번호 확인
                </label>
                <input
                  type="password"
                  id="confirmPassword"
                  name="confirmPassword"
                  placeholder="비밀번호 재입력"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  className={`w-full p-3 bg-gray-100 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                    errors.confirmPassword
                      ? "border-red-500"
                      : "border-gray-300"
                  }`}
                />
                {errors.confirmPassword && (
                  <p className="mt-1 text-red-500 text-sm">
                    {errors.confirmPassword}
                  </p>
                )}
              </div>

              <div className="mb-6">
                <label
                  htmlFor="nickname"
                  className="block text-gray-700 text-lg font-medium mb-2"
                >
                  닉네임
                </label>
                <input
                  type="text"
                  id="nickname"
                  name="nickname"
                  placeholder="다른 사용자에게 표시될 이름"
                  value={formData.nickname}
                  onChange={handleChange}
                  className={`w-full p-3 bg-gray-100 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                    errors.nickname ? "border-red-500" : "border-gray-300"
                  }`}
                />
                {errors.nickname && (
                  <p className="mt-1 text-red-500 text-sm">{errors.nickname}</p>
                )}
              </div>

              <div className="mb-6">
                <label
                  htmlFor="gender"
                  className="block text-gray-700 text-lg font-medium mb-2"
                >
                  성별
                </label>
                <select
                  id="gender"
                  name="gender"
                  value={formData.gender}
                  onChange={handleChange}
                  className="w-full p-3 bg-gray-100 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="MALE">남성</option>
                  <option value="FEMALE">여성</option>
                </select>
              </div>

              <div className="mb-6">
                <label
                  htmlFor="ageRange"
                  className="block text-gray-700 text-lg font-medium mb-2"
                >
                  연령대
                </label>
                <select
                  id="ageRange"
                  name="ageRange"
                  value={formData.ageRange}
                  onChange={handleChange}
                  className="w-full p-3 bg-gray-100 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="TEENS">10대</option>
                  <option value="TWENTIES">20대</option>
                  <option value="THIRTIES">30대</option>
                  <option value="FORTIES_PLUS">40대 이상</option>
                </select>
              </div>

              <div className="mb-6">
                <label
                  htmlFor="travelStyle"
                  className="block text-gray-700 text-lg font-medium mb-2"
                >
                  여행 스타일
                </label>
                <select
                  id="travelStyle"
                  name="travelStyle"
                  value={formData.travelStyle}
                  onChange={handleChange}
                  className="w-full p-3 bg-gray-100 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="TOURISM">관광</option>
                  <option value="RELAXATION">휴양</option>
                  <option value="SHOPPING">쇼핑</option>
                  <option value="ADVENTURE">액티비티</option>
                  <option value="GOURMET">미식</option>
                </select>
              </div>

              <div className="mb-6">
                <label
                  htmlFor="aboutMe"
                  className="block text-gray-700 text-lg font-medium mb-2"
                >
                  자기소개
                </label>
                <textarea
                  id="aboutMe"
                  name="aboutMe"
                  placeholder="자신을 소개해주세요 (선택사항)"
                  value={formData.aboutMe}
                  onChange={handleChange}
                  className="w-full p-3 bg-gray-100 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 h-32"
                ></textarea>
              </div>
            </div>

            <button
              type="submit"
              className="w-full bg-blue-600 text-white py-3 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors"
            >
              이메일 인증하기
            </button>
          </form>
        </div>
      </div>

      {/* 회원가입 안내 섹션 */}
      <div className="container mx-auto px-4 py-12">
        <div className="max-w-4xl mx-auto">
          <h3 className="text-2xl font-bold text-center mb-8">회원가입 안내</h3>
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

      <Footer />
    </div>
  );
}
