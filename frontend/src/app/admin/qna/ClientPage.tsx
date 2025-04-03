"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import api from "@/lib/api";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import { Question } from "@/types/qna";

export default function ClientPage() {
  const [questions, setQuestions] = useState<Question[]>([]);

  useEffect(() => {
    const fetchQuestions = async () => {
      try {
        const res = await api.get("/admin/qna/questions");
        setQuestions(res.data);
      } catch (error) {
        console.error("Q&A 목록 조회 실패", error);
      }
    };
    fetchQuestions();
  }, []);

  const handleDelete = async (id: number) => {
    if (!confirm("정말 삭제하시겠습니까?")) return;
    try {
      await api.delete(`/admin/qna/questions/${id}`);
      setQuestions((prev) => prev.filter((q) => q.id !== id));
    } catch (error) {
      console.error("질문 삭제 실패", error);
    }
  };

  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-grow container mx-auto px-4 py-10">
        <h1 className="text-3xl font-bold mb-6">Q&A 관리</h1>
        {questions.length === 0 ? (
          <p className="text-gray-500">등록된 질문이 없습니다.</p>
        ) : (
          <ul className="space-y-4">
            {questions.map((q) => (
              <li key={q.id} className="bg-white p-4 border rounded shadow">
                <Link href={`/admin/qna/${q.id}`}>
                  <h2 className="text-xl font-semibold text-blue-600 hover:underline">
                    {q.title}
                  </h2>
                </Link>
                <p className="text-gray-700 mt-2">{q.content}</p>
                <p className="text-sm text-gray-500 mt-1">
                  작성자: {q.memberUsername} |{" "}
                  {new Date(q.createdAt).toLocaleString()}
                </p>
                <button
                  onClick={() => handleDelete(q.id)}
                  className="mt-3 text-red-500 text-sm hover:underline"
                >
                  삭제
                </button>
              </li>
            ))}
          </ul>
        )}
      </main>
      <Footer />
    </div>
  );
}
