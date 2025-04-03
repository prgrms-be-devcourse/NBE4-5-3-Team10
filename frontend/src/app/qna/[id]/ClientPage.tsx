"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import api from "@/lib/api";
import AnswerSection from "./AnswerSection"; 

export type Question = {
  id: number;
  title: string;
  content: string;
  memberUsername: string;
  createdAt: string;
};

export default function ClientPage() {
  const { id } = useParams();
  const [question, setQuestion] = useState<Question | null>(null);

  useEffect(() => {
    if (!id) return;
    const fetchData = async () => {
      try {
        const res = await api.get(`/qna/${id}`);
        setQuestion(res.data);
      } catch (err) {
        console.error("질문 로딩 실패", err);
      }
    };
    fetchData();
  }, [id]);

  if (!question) return <p>Loading...</p>;

  return (
    <div className="p-8 max-w-3xl mx-auto">
      <h1 className="text-2xl font-bold mb-4">{question.title}</h1>
      <p className="mb-6">{question.content}</p>
      <p className="text-sm text-gray-500 mb-8">
        작성자: {question.memberUsername} |{" "}
        {new Date(question.createdAt).toLocaleDateString()}
      </p>

      {/* ✅ 답변 영역 컴포넌트 */}
      <AnswerSection questionId={question.id} />
    </div>
  );
}
