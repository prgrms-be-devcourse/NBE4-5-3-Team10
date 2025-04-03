"use client";

import { useEffect, useState } from "react";
import {
  fetchAllEvents,
  createEvent,
  updateEvent,
  deleteEvent,
} from "@/lib/api/event";
import Header from "@/components/Header";
import Footer from "@/components/Footer";

type Event = {
  id: number;
  title: string;
  description: string;
  eventDate: string; // YYYY-MM-DD
  createdAt: string;
};

export default function ClientPage() {
  const [events, setEvents] = useState<Event[]>([]);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [eventDate, setEventDate] = useState("");
  const [editingId, setEditingId] = useState<number | null>(null);

  const loadEvents = async () => {
    try {
      const data = await fetchAllEvents();
      console.log("📦 받아온 이벤트 데이터:", data);
      setEvents(data);
    } catch (err) {
      console.error("이벤트 불러오기 실패", err);
    }
  };

  useEffect(() => {
    loadEvents();
  }, []);

  const handleSubmit = async () => {
    if (!title || !description || !eventDate) {
      alert("모든 항목을 입력해주세요.");
      return;
    }

    try {
      if (editingId !== null) {
        await updateEvent(editingId, { title, description, eventDate });
      } else {
        await createEvent({ title, description, eventDate });
      }

      setTitle("");
      setDescription("");
      setEventDate("");
      setEditingId(null);

      loadEvents();
    } catch (err) {
      console.error("이벤트 저장 실패", err);
    }
  };

  const handleEdit = (e: Event) => {
    setTitle(e.title);
    setDescription(e.description);
    setEventDate(e.eventDate);
    setEditingId(e.id);
  };

  const handleDelete = async (id: number) => {
    if (confirm("정말 삭제하시겠습니까?")) {
      try {
        await deleteEvent(id);
        loadEvents();
      } catch (err) {
        console.error("삭제 실패", err);
      }
    }
  };

  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-grow max-w-3xl mx-auto p-8">
        <h1 className="text-2xl font-bold mb-6">🎉 이벤트 관리 페이지</h1>

        <div className="space-y-2 mb-8">
          <input
            className="w-full p-2 border rounded"
            placeholder="이벤트 제목"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
          <textarea
            className="w-full p-2 border rounded h-24"
            placeholder="이벤트 설명"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
          <input
            type="date"
            className="w-full p-2 border rounded"
            value={eventDate}
            onChange={(e) => setEventDate(e.target.value)}
          />
          <button
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
            onClick={handleSubmit}
          >
            {editingId ? "수정하기" : "등록하기"}
          </button>
        </div>

        {events.length === 0 ? (
          <p>등록된 이벤트가 없습니다.</p>
        ) : (
          <ul className="space-y-4">
            {events.map((e) => (
              <li
                key={e.id}
                className="border rounded p-4 flex justify-between items-start"
              >
                <div>
                  <h3 className="font-bold text-lg">{e.title}</h3>
                  <p className="text-sm text-gray-700">{e.description}</p>
                  <p className="text-sm text-blue-600 mt-1">
                    📅 이벤트 날짜: {e.eventDate}
                  </p>
                  <p className="text-xs text-gray-500 mt-1">
                    등록일: {new Date(e.createdAt).toLocaleString()}
                  </p>
                </div>
                <div className="flex gap-2">
                  <button
                    className="text-sm text-blue-600"
                    onClick={() => handleEdit(e)}
                  >
                    수정
                  </button>
                  <button
                    className="text-sm text-red-600"
                    onClick={() => handleDelete(e.id)}
                  >
                    삭제
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </main>

      <Footer />
    </div>
  );
}
