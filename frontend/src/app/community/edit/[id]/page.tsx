import { Suspense } from "react"
import type { Metadata } from "next"
import ReviewForm from "../../review-form"
import EditAuthCheck from "../../components/edit-auth-check"

interface EditReviewPageProps {
  params: {
    id: string
  }
}

export const metadata: Metadata = {
  title: "TripFriend - 리뷰 수정",
  description: "작성한 여행 리뷰를 수정합니다.",
}

export default function EditReviewPage({ params }: EditReviewPageProps) {
  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-blue-600 text-white">
        <div className="container mx-auto px-4 py-12 md:py-16">
          <div className="max-w-4xl">
            <h1 className="text-3xl md:text-4xl font-bold mb-4">리뷰 수정하기</h1>
            <p className="text-xl">작성한 여행 리뷰를 수정합니다.</p>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          <Suspense fallback={<div className="text-center py-10">리뷰 정보를 불러오는 중...</div>}>
            <EditAuthCheck reviewId={params.id}>
              <ReviewForm reviewId={params.id} />
            </EditAuthCheck>
          </Suspense>
        </div>
      </div>
    </div>
  )
}