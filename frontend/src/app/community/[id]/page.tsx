import { Suspense } from "react";
import type { Metadata } from "next";
import ReviewDetail from "../review-detail";

interface ReviewDetailPageProps {
  params: {
    id: string;
  };
}

export const metadata: Metadata = {
  title: "TripFriend - 여행 리뷰",
  description: "여행자들의 생생한 리뷰와 경험을 공유하는 공간입니다.",
};

// 서버 컴포넌트를 async로 선언
export default async function ReviewDetailPage({
  params,
}: ReviewDetailPageProps) {
  // params 객체가 완전히 로드될 때까지 기다립니다
  const resolvedParams = await Promise.resolve(params);
  const id = resolvedParams.id;

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        <Suspense
          fallback={
            <div className="text-center py-10">리뷰를 불러오는 중...</div>
          }
        >
          <ReviewDetail id={id} />
        </Suspense>
      </div>
    </div>
  );
}
