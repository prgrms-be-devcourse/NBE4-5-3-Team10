"use client"
import Link from "next/link"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"

export default function LocalHeader() {
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const router = useRouter()

  // Check login status when component mounts and when component updates
  useEffect(() => {
    const checkLoginStatus = () => {
      const token = localStorage.getItem("accessToken")
      setIsLoggedIn(!!token)
    }

    // Initial check
    checkLoginStatus()

    // Set up event listeners
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === "accessToken" || e.key === null) {
        checkLoginStatus()
      }
    }

    window.addEventListener("storage", handleStorageChange)
    window.addEventListener("login", checkLoginStatus)
    window.addEventListener("logout", checkLoginStatus)

    // Cleanup
    return () => {
      window.removeEventListener("storage", handleStorageChange)
      window.removeEventListener("login", checkLoginStatus)
      window.removeEventListener("logout", checkLoginStatus)
    }
  }, [])

  const handleLogout = async () => {
    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"
      const logoutUrl = `${apiUrl}/member/logout`

      console.log("로그아웃 요청 URL:", logoutUrl)

      // First remove tokens from localStorage to ensure client-side logout happens
      localStorage.removeItem("accessToken")
      localStorage.removeItem("refreshToken")

      // Update state immediately
      setIsLoggedIn(false)

      // Call the Spring logout API endpoint
      const response = await fetch(logoutUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include", // Important to include cookies
      })

      console.log("로그아웃 응답 상태:", response.status)

      // Dispatch a custom event for logout
      window.dispatchEvent(new Event("logout"))

      // Force a page refresh to ensure all components update
      router.push("/")
    } catch (error) {
      console.error("Error during logout:", error)

      // Notify user (optional)
      alert("로그아웃 처리 중 문제가 발생했습니다. 하지만 로그아웃되었습니다.")

      // Redirect anyway
      router.push("/")
    }
  }

  return (
      <nav className="bg-white shadow-md">
        <div className="container mx-auto px-4 py-3 flex justify-between items-center">
          <div className="flex items-center">
            <Link href="/">
              <h1 className="text-2xl font-bold text-blue-600 cursor-pointer">TripFriend</h1>
            </Link>
          </div>
          <div className="hidden md:flex space-x-6">
            <Link href="/trips" className="text-gray-700 hover:text-blue-600">
              여행 동행 찾기
            </Link>
            <Link href="/create" className="text-gray-700 hover:text-blue-600">
              동행 등록하기
            </Link>
            <Link href="/community" className="text-gray-700 hover:text-blue-600">
              커뮤니티
            </Link>
            <Link href="/about" className="text-gray-700 hover:text-blue-600">
              서비스 소개
            </Link>
          </div>
          <div className="flex items-center space-x-4">
            {isLoggedIn ? (
                <>
                  <button onClick={handleLogout} className="text-gray-700 hover:text-blue-600">
                    로그아웃
                  </button>
                  <Link href="/member/my" className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700">
                    마이페이지
                  </Link>
                </>
            ) : (
                <>
                  <Link href="/member/login" className="text-gray-700 hover:text-blue-600">
                    로그인
                  </Link>
                  <Link href="/member/signup" className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700">
                    회원가입
                  </Link>
                </>
            )}
          </div>
        </div>
      </nav>
  )
}

