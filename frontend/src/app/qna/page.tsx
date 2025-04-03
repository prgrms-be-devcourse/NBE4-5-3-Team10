"use client";

import { useEffect, useState } from "react";
import api from "@/lib/api";
import Link from "next/link";
import Header from "@/components/Header";
import Footer from "@/components/Footer";

type Question = {
  id: number;
  title: string;
  content: string;
  createdAt: string;
  memberUsername: string;
};

const ITEMS_PER_PAGE = 5;

export default function QnaPage() {
  const [questions, setQuestions] = useState<Question[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentUsername, setCurrentUsername] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    const fetchQuestions = async () => {
      try {
        const res = await api.get("/qna");
        setQuestions(res.data);
      } catch (err) {
        console.error("질문 목록을 불러오지 못했습니다.", err);
      } finally {
        setLoading(false);
      }
    };

    const fetchCurrentUser = async () => {
      try {
        const token = localStorage.getItem("accessToken");
        if (!token) return;

        const res = await api.get("/member/me", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        setCurrentUsername(res.data.username);
      } catch (err) {
        console.error("현재 사용자 정보 조회 실패", err);
      }
    };

    fetchQuestions();
    fetchCurrentUser();
  }, []);

  const handleAskQuestion = () => {
    const token = localStorage.getItem("accessToken");

    if (!token) {
      setErrorMessage("질문을 작성하려면 로그인해주세요.");
      setTimeout(() => setErrorMessage(""), 4000);
      return;
    }

    window.location.href = "/qna/question";
  };

  const totalPages = Math.ceil(questions.length / ITEMS_PER_PAGE);
  const paginatedQuestions = questions.slice(
    (currentPage - 1) * ITEMS_PER_PAGE,
    currentPage * ITEMS_PER_PAGE
  );

  const handleDelete = async (questionId: number) => {
    const confirm = window.confirm("정말 이 질문을 삭제하시겠습니까?");
    if (!confirm) return;

    try {
      const token = localStorage.getItem("accessToken");
      await api.delete(`/qna/${questionId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      const res = await api.get("/qna");
      setQuestions(res.data);
    } catch (err) {
      console.error("질문 삭제 실패", err);
      alert("질문 삭제에 실패했습니다.");
    }
  };

  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-grow max-w-4xl mx-auto p-8">
        {errorMessage && (
          <div className="mb-4 p-3 bg-red-100 text-red-700 border border-red-300 rounded text-center">
            {errorMessage}
          </div>
        )}

        <div className="flex justify-between items-center mb-10">
          <h1 className="text-3xl font-bold">💬 Q&A</h1>
          <button
            onClick={handleAskQuestion}
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
          >
            질문하기
          </button>
        </div>

        {loading ? (
          <p>불러오는 중...</p>
        ) : paginatedQuestions.length === 0 ? (
          <p>등록된 질문이 없습니다.</p>
        ) : (
          <ul className="space-y-6">
            {paginatedQuestions.map((q) => (
              <li
                key={q.id}
                className="bg-blue-50 border-l-4 border-blue-500 rounded shadow-sm p-5 max-w-2xl mx-auto"
              >
                <Link href={`/qna/${q.id}`}>
                  <h2 className="text-lg font-bold text-blue-600 hover:underline">
                    {q.title}
                  </h2>
                </Link>
                <p className="text-gray-700 mt-2">{q.content}</p>
                <div className="text-sm text-gray-500 mt-2">
                  {q.memberUsername} · {new Date(q.createdAt).toLocaleString()}
                </div>
                {q.memberUsername === currentUsername && (
                  <button
                    onClick={() => handleDelete(q.id)}
                    className="mt-2 text-sm text-red-500 hover:underline"
                  >
                    삭제
                  </button>
                )}
              </li>
            ))}
          </ul>
        )}

        {!loading && totalPages > 1 && (
          <div className="flex justify-center mt-8 space-x-2">
            {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
              <button
                key={page}
                onClick={() => setCurrentPage(page)}
                className={`px-3 py-1 rounded text-sm ${
                  page === currentPage
                    ? "bg-blue-600 text-white"
                    : "bg-gray-200 text-gray-700"
                }`}
              >
                {page}
              </button>
            ))}
          </div>
        )}
      </main>

      <Footer />
    </div>
  );
}
