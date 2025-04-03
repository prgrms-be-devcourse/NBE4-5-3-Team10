"use client"

import { ReactNode, useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { isLoggedIn } from "../services/authService"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { AlertCircle } from "lucide-react"
import { Button } from "@/components/ui/button"

interface WriteAuthCheckProps {
    children: ReactNode
}

export default function WriteAuthCheck({ children }: WriteAuthCheckProps) {
    const router = useRouter()
    const [userLoggedIn, setUserLoggedIn] = useState<boolean | null>(null)

    useEffect(() => {
        // 로그인 상태 확인
        const loggedIn = isLoggedIn()
        setUserLoggedIn(loggedIn)

        // 로그인 이벤트 리스너 등록
        const handleLogin = () => setUserLoggedIn(true)
        const handleLogout = () => setUserLoggedIn(false)

        window.addEventListener("login", handleLogin)
        window.addEventListener("logout", handleLogout)

        return () => {
            window.removeEventListener("login", handleLogin)
            window.removeEventListener("logout", handleLogout)
        }
    }, [])

    // 로그인 상태 확인 중
    if (userLoggedIn === null) {
        return <div className="text-center py-10">확인 중...</div>
    }

    // 로그인되지 않은 경우
    if (!userLoggedIn) {
        return (
            <div className="space-y-6">
                <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertTitle>로그인 필요</AlertTitle>
                    <AlertDescription>
                        리뷰를 작성하려면 로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?
                    </AlertDescription>
                </Alert>
                <div className="flex justify-center space-x-4">
                    <Button variant="default" onClick={() => router.push("/member/login")}>
                        로그인하기
                    </Button>
                    <Button variant="outline" onClick={() => router.push("/community")}>
                        돌아가기
                    </Button>
                </div>
            </div>
        )
    }

    // 로그인된 경우 자식 컴포넌트 렌더링
    return <>{children}</>
}