"use client";

import { useEffect, useState } from "react";
import { useRouter, useParams } from "next/navigation";
import api from "@/lib/api";
import Header from "@/components/Header";
import Footer from "@/components/Footer";

type QuestionDetail = {
  id: number;
  title: string;
  content: string;
  memberUsername: string;
  createdAt: string;
  answers: {
    answerId: number;
    content: string;
    memberUsername: string;
    createdAt: string;
  }[];
};

export default function AdminQnaDetailPage() {
  const { id } = useParams();
  const router = useRouter();
  const [question, setQuestion] = useState<QuestionDetail | null>(null);

  useEffect(() => {
    const fetchQuestionDetail = async () => {
      try {
        const res = await api.get(`/admin/qna/questions/${id}`);
        setQuestion(res.data);
      } catch (err) {
        console.error("질문 상세 조회 실패", err);
      }
    };

    fetchQuestionDetail();
  }, [id]);

  const handleDeleteAnswer = async (answerId: number) => {
    if (!confirm("이 답변을 삭제하시겠습니까?")) return;
    try {
      await api.delete(`/admin/qna/answers/${answerId}`);
      setQuestion((prev) =>
        prev
          ? {
              ...prev,
              answers: prev.answers.filter((a) => a.answerId !== answerId),
            }
          : prev
      );
    } catch (err) {
      console.error("답변 삭제 실패", err);
    }
  };

  const handleDeleteQuestion = async () => {
    if (!confirm("이 질문을 삭제하시겠습니까?")) return;
    try {
      await api.delete(`/admin/qna/questions/${id}`);
      router.push("/admin/qna");
    } catch (err) {
      console.error("질문 삭제 실패", err);
    }
  };

  if (!question) return null;

  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-grow container mx-auto px-4 py-10">
        <h1 className="text-3xl font-bold mb-6">Q&A 상세</h1>

        <div className="bg-white border rounded p-6 shadow mb-8">
          <h2 className="text-2xl font-semibold">{question.title}</h2>
          <p className="mt-2">{question.content}</p>
          <p className="text-sm text-gray-500 mt-2">
            작성자: {question.memberUsername} |{" "}
            {new Date(question.createdAt).toLocaleString()}
          </p>

          <button
            onClick={handleDeleteQuestion}
            className="mt-4 text-red-500 hover:underline text-sm"
          >
            질문 삭제
          </button>
        </div>

        <h3 className="text-xl font-bold mb-4">답변 목록</h3>
        {question.answers.length === 0 ? (
          <p className="text-gray-500">등록된 답변이 없습니다.</p>
        ) : (
          <ul className="space-y-4">
            {question.answers.map((answer) => (
              <li
                key={answer.answerId}
                className="bg-gray-50 p-4 border rounded"
              >
                <p>{answer.content}</p>
                <p className="text-sm text-gray-500 mt-1">
                  작성자: {answer.memberUsername} |{" "}
                  {new Date(answer.createdAt).toLocaleString()}
                </p>
                <button
                  onClick={() => handleDeleteAnswer(answer.answerId)}
                  className="mt-2 text-red-500 text-sm hover:underline"
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
