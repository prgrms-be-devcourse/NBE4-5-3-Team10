import { Suspense } from "react"
import ReviewList from "./review-list"
import type { Metadata } from "next"

export const metadata: Metadata = {
  title: "TripFriend - 커뮤니티",
  description: "여행자들의 생생한 리뷰와 경험을 공유하는 공간입니다.",
}

export default function CommunityPage() {
  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-blue-600 text-white">
        <div className="container mx-auto px-4 py-12 md:py-16">
          <div className="max-w-4xl">
            <h1 className="text-3xl md:text-5xl font-bold mb-4">여행 리뷰 커뮤니티</h1>
            <p className="text-xl">
              다양한 여행자들의 생생한 경험과 리뷰를 확인하고 나만의 여행 이야기를 공유해보세요.
            </p>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <Suspense fallback={<div className="text-center py-10">리뷰를 불러오는 중...</div>}>
          <ReviewList />
        </Suspense>
      </div>
    </div>
  )
}