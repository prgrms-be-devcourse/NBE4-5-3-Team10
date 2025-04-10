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
// 서버 컴포넌트를 async로 선언
export default async function EditReviewPage({ params }: EditReviewPageProps) {
  // 동적 라우트 파라미터에 접근하기 전에 async/await 패턴을 사용
  // params 객체가 완전히 로드될 때까지 기다립니다
  const resolvedParams = await Promise.resolve(params);
  const id = resolvedParams.id;
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
            <EditAuthCheck reviewId={id}>
              <ReviewForm reviewId={id} />
            </EditAuthCheck>
          </Suspense>
        </div>
      </div>
    </div>
  )
}