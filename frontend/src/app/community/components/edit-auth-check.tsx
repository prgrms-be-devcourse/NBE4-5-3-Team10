"use client"

import { ReactNode, useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { getCurrentUserInfo, isLoggedIn } from "../services/authService"
import { getReviewById } from "../services/reviewService"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { AlertCircle } from "lucide-react"
import { Button } from "@/components/ui/button"

interface EditAuthCheckProps {
    children: ReactNode
    reviewId: string
}

export default function EditAuthCheck({ children, reviewId }: EditAuthCheckProps) {
    const router = useRouter()
    const [loading, setLoading] = useState(true)
    const [authorized, setAuthorized] = useState(false)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        const checkAuthorization = async () => {
            setLoading(true)

            // 로그인 여부 확인
            if (!isLoggedIn()) {
                setError("리뷰 수정을 위해 로그인이 필요합니다.")
                setLoading(false)
                return
            }

            try {
                // 현재 사용자 정보 가져오기
                const userInfo = await getCurrentUserInfo()
                if (!userInfo) {
                    setError("사용자 정보를 불러올 수 없습니다.")
                    setLoading(false)
                    return
                }

                // 리뷰 정보 가져오기
                const review = await getReviewById(parseInt(reviewId))

                // 현재 사용자가 작성자인지 확인
                if (review.memberId === userInfo.id) {
                    setAuthorized(true)
                } else {
                    setError("리뷰 작성자만 수정할 수 있습니다.")
                }
            } catch (err) {
                console.error("권한 확인 중 오류가 발생했습니다:", err)
                setError("리뷰 정보를 불러오는 중 오류가 발생했습니다.")
            } finally {
                setLoading(false)
            }
        }

        checkAuthorization()
    }, [reviewId])

    if (loading) {
        return <div className="text-center py-10">권한 확인 중...</div>
    }

    if (error || !authorized) {
        return (
            <div className="space-y-6">
                <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertTitle>접근 권한 없음</AlertTitle>
                    <AlertDescription>
                        {error || "이 페이지에 접근할 수 없습니다."}
                    </AlertDescription>
                </Alert>
                <div className="flex justify-center space-x-4">
                    {!isLoggedIn() && (
                        <Button variant="default" onClick={() => router.push("/member/login")}>
                            로그인하기
                        </Button>
                    )}
                    <Button variant="outline" onClick={() => router.push(`/community/${reviewId}`)}>
                        리뷰 보기
                    </Button>
                    <Button variant="outline" onClick={() => router.push("/community")}>
                        목록으로
                    </Button>
                </div>
            </div>
        )
    }

    return <>{children}</>
}