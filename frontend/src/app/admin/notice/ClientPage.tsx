"use client";

import { useEffect, useState } from "react";
import {
  fetchAllNotices,
  createNotice,
  updateNotice,
  deleteNotice,
} from "@/api/notice";

export type Notice = {
  id: number;
  title: string;
  content: string;
  createdAt: string;
  updatedAt: string;
};

export default function ClientPage() {
  const [notices, setNotices] = useState<Notice[]>([]);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [editingId, setEditingId] = useState<number | null>(null);

  const loadNotices = async () => {
    try {
      const data = await fetchAllNotices();
      setNotices(data);
    } catch (err) {
      console.error("공지 불러오기 실패", err);
    }
  };

  useEffect(() => {
    loadNotices();
  }, []);

  const handleSubmit = async () => {
    try {
      if (editingId !== null) {
        await updateNotice(editingId, { title, content });
      } else {
        await createNotice({ title, content });
      }
      setTitle("");
      setContent("");
      setEditingId(null);
      loadNotices();
    } catch (err) {
      console.error("공지 저장 실패", err);
    }
  };

  const handleEdit = (notice: Notice) => {
    setTitle(notice.title);
    setContent(notice.content);
    setEditingId(notice.id);
  };

  const handleDelete = async (id: number) => {
    if (confirm("정말 삭제하시겠습니까?")) {
      try {
        await deleteNotice(id);
        loadNotices();
      } catch (err) {
        console.error("삭제 실패", err);
      }
    }
  };

  return (
    <div className="max-w-3xl mx-auto p-8">
      <h1 className="text-2xl font-bold mb-6">📢 관리자 공지 페이지</h1>

      {/* 등록 / 수정 폼 */}
      <div className="space-y-2 mb-8">
        <input
          className="w-full p-2 border rounded"
          placeholder="제목"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
        <textarea
          className="w-full p-2 border rounded h-24"
          placeholder="내용"
          value={content}
          onChange={(e) => setContent(e.target.value)}
        />
        <button
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
          onClick={handleSubmit}
        >
          {editingId ? "수정하기" : "등록하기"}
        </button>
      </div>

      {/* 목록 */}
      {notices.length === 0 ? (
        <p>공지사항이 없습니다.</p>
      ) : (
        <ul className="space-y-4">
          {notices.map((n) => (
            <li
              key={n.id}
              className="border rounded p-4 flex justify-between items-start"
            >
              <div>
                <h3 className="font-bold">{n.title}</h3>
                <p>{n.content}</p>
                <p className="text-xs text-gray-500">
                  {new Date(n.createdAt).toLocaleString()}
                </p>
              </div>
              <div className="flex gap-2">
                <button
                  className="text-sm text-blue-600"
                  onClick={() => handleEdit(n)}
                >
                  수정
                </button>
                <button
                  className="text-sm text-red-600"
                  onClick={() => handleDelete(n.id)}
                >
                  삭제
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
