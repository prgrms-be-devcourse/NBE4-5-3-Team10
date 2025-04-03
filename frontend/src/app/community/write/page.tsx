import type { Metadata } from "next"
import { Suspense } from "react"
import ReviewForm from "../review-form"
import WriteAuthCheck from "../components/write-auth-check"

export const metadata: Metadata = {
  title: "TripFriend - 리뷰 작성",
  description: "여행 경험을 공유하고 다른 여행자들에게 도움을 주세요.",
}

export default function WriteReviewPage() {
  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-blue-600 text-white">
        <div className="container mx-auto px-4 py-12 md:py-16">
          <div className="max-w-4xl">
            <h1 className="text-3xl md:text-4xl font-bold mb-4">리뷰 작성하기</h1>
            <p className="text-xl">여행 경험을 공유하고 다른 여행자들에게 도움을 주세요.</p>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          <Suspense fallback={<div className="text-center py-10">로딩 중...</div>}>
            <WriteAuthCheck>
              <ReviewForm />
            </WriteAuthCheck>
          </Suspense>
        </div>
      </div>
    </div>
  )
}