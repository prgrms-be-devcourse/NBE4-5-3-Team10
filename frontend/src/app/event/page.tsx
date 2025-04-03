"use client";

import { useEffect, useState } from "react";
import axios from "@/lib/axios";
import Header from "@/components/Header";
import Footer from "@/components/Footer";

type Event = {
  id: number;
  title: string;
  description: string;
  eventDate: string;
  createdAt: string;
};

export default function PublicEventPage() {
  const [events, setEvents] = useState<Event[]>([]);

  useEffect(() => {
    const loadEvents = async () => {
      try {
        const res = await axios.get("/admin/event");
        setEvents(res.data);
      } catch (err) {
        console.error("이벤트 불러오기 실패", err);
      }
    };

    loadEvents();
  }, []);

  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-grow w-full max-w-7xl mx-auto px-6 py-10">
        <h1 className="text-3xl font-bold mb-10 text-center">🎉 TripFriend 이벤트</h1>

        {Array.isArray(events) && events.length > 0 ? (
          events.length === 1 ? (
            <ul className="flex justify-center">
              <li className="w-full max-w-md border rounded-lg p-6 shadow-sm hover:shadow-md transition bg-white">
                <h3 className="text-xl font-semibold mb-2">{events[0].title}</h3>
                <p className="text-sm text-gray-700 mb-1">{events[0].description}</p>
                <p className="text-sm text-blue-600">
                  📅 이벤트 날짜: {events[0].eventDate}
                </p>
                <p className="text-xs text-gray-500 mt-1">
                  등록일: {new Date(events[0].createdAt).toLocaleString()}
                </p>
              </li>
            </ul>
          ) : (
            <ul className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {events.map((e) => (
                <li
                  key={e.id}
                  className="border rounded-lg p-6 shadow-sm hover:shadow-md transition bg-white"
                >
                  <h3 className="text-xl font-semibold mb-2">{e.title}</h3>
                  <p className="text-sm text-gray-700 mb-1">{e.description}</p>
                  <p className="text-sm text-blue-600">📅 이벤트 날짜: {e.eventDate}</p>
                  <p className="text-xs text-gray-500 mt-1">
                    등록일: {new Date(e.createdAt).toLocaleString()}
                  </p>
                </li>
              ))}
            </ul>
          )
        ) : (
          <p className="text-center text-gray-500">진행 중인 이벤트가 없습니다.</p>
        )}
      </main>

      <Footer />
    </div>
  );
}
