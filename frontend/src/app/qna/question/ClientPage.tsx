"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import api from "@/lib/api";

export default function QuestionWritePage() {
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [isEditMode, setIsEditMode] = useState(false);
  const searchParams = useSearchParams();
  const questionId = searchParams.get("id"); // 주소에 ?id=1 이런 식으로 넘김
  const router = useRouter();

  useEffect(() => {
    if (questionId) {
      setIsEditMode(true);
      fetchQuestion();
    }
  }, [questionId]);

  const fetchQuestion = async () => {
    try {
      const res = await api.get(`/qna/${questionId}`);
      setTitle(res.data.title);
      setContent(res.data.content);
    } catch (err) {
      console.error("질문 로딩 실패", err);
    }
  };

  const handleSubmit = async () => {
    try {
      const token = localStorage.getItem("accessToken");

      if (isEditMode) {
        // 수정 로직
        await api.put(
          `/qna/${questionId}`,
          { title, content },
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );
        alert("질문이 수정되었습니다.");
      } else {
        // 등록 로직
        await api.post(
          "/qna/question",
          { title, content },
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );
        alert("질문이 등록되었습니다.");
      }

      router.push("/qna");
    } catch (err) {
      console.error("질문 등록/수정 실패", err);
      alert("작업 실패");
    }
  };

  const handleDelete = async () => {
    if (!window.confirm("정말 삭제하시겠습니까?")) return;

    try {
      const token = localStorage.getItem("accessToken");

      await api.delete(`/qna/${questionId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      alert("질문이 삭제되었습니다.");
      router.push("/qna");
    } catch (err) {
      console.error("질문 삭제 실패", err);
      alert("삭제 실패");
    }
  };

  return (
    <div className="max-w-3xl mx-auto p-8">
      <h1 className="text-2xl font-bold mb-6">
        {isEditMode ? "질문 수정" : "질문 작성"}
      </h1>

      <input
        type="text"
        className="w-full border px-4 py-2 mb-4 rounded"
        placeholder="질문 제목"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
      />
      <textarea
        className="w-full border px-4 py-2 h-40 rounded"
        placeholder="질문 내용을 입력하세요"
        value={content}
        onChange={(e) => setContent(e.target.value)}
      />

      <div className="mt-4 flex gap-4">
        <button
          className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700"
          onClick={handleSubmit}
        >
          {isEditMode ? "질문 수정" : "질문 등록"}
        </button>

        {isEditMode && (
          <button
            className="bg-red-500 text-white px-6 py-2 rounded hover:bg-red-600"
            onClick={handleDelete}
          >
            삭제
          </button>
        )}
      </div>
    </div>
  );
}
