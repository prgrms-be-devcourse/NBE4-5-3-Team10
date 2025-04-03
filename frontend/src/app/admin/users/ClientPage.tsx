"use client";

import { useEffect, useState } from "react";
import { fetchAllMembers, Member } from "@/api/member";
import Header from "@/components/Header";
import Footer from "@/components/Footer";

export default function ClientPage() {
  const [members, setMembers] = useState<Member[]>([]);

  useEffect(() => {
    const loadMembers = async () => {
      try {
        const data = await fetchAllMembers();
        setMembers(data);
      } catch (err) {
        console.error("회원 목록 불러오기 실패", err);
      }
    };

    loadMembers();
  }, []);

  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-grow max-w-6xl mx-auto px-6 py-10">
        <h1 className="text-3xl font-bold mb-10 text-center">👥 회원 목록</h1>

        {members.length === 0 ? (
          <p className="text-center text-gray-500">등록된 회원이 없습니다.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full border-collapse border text-base text-left">
              <thead>
                <tr className="border-b bg-gray-100">
                  <th className="p-4">ID</th>
                  <th className="p-4">아이디</th>
                  <th className="p-4">이메일</th>
                  <th className="p-4">닉네임</th>
                </tr>
              </thead>
              <tbody>
                {members.map((m) => (
                  <tr key={m.id} className="border-b hover:bg-gray-50">
                    <td className="p-4">{m.id}</td>
                    <td className="p-4">{m.username}</td>
                    <td className="p-4">{m.email}</td>
                    <td className="p-4">{m.nickname}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>

      <Footer />
    </div>
  );
}
