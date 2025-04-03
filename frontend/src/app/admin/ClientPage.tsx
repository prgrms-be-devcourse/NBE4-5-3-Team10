"use client";

import Link from "next/link";
import Header from "@/components/Header";
import Footer from "@/components/Footer";

export default function ClientPage() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      {/* 관리자 대시보드 Hero 섹션 */}
      <div className="bg-blue-600 text-white py-16">
        <div className="container mx-auto px-4 text-center">
          <h1 className="text-4xl font-bold mb-4">TripFriend 관리자 페이지</h1>
          <p className="text-xl">
            여행지, 게시글, 회원, 블랙리스트 등 서비스를 직접 관리할 수
            있습니다.
          </p>
        </div>
      </div>

      {/* 관리자 기능 목록 */}
      <div className="container mx-auto px-4 py-12">
        <h2 className="text-2xl font-bold mb-8">관리 기능</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <AdminCard title="게시글 관리" href="/admin/post" />
          <AdminCard title="여행지 관리" href="/admin/place" />
          <AdminCard title="이벤트 관리" href="/admin/event" />
          <AdminCard title="공지사항 관리" href="/admin/notice" />
          <AdminCard title="회원 목록" href="/admin/users" />
          <AdminCard title="블랙리스트 관리" href="/admin/blacklist" />
          <AdminCard title="Q&A 관리" href="/admin/qna" />
        </div>
      </div>

      <Footer />
    </div>
  );
}

function AdminCard({ title, href }: { title: string; href: string }) {
  return (
    <Link
      href={href}
      className="block bg-white rounded-lg shadow-md hover:shadow-lg p-6 transition duration-200 border"
    >
      <h3 className="text-xl font-semibold text-gray-800">{title}</h3>
      <p className="text-gray-600 mt-2">자세히 보기 →</p>
    </Link>
  );
}
