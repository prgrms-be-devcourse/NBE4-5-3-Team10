// app/admin/blacklist/page.tsx
"use client";

import { useEffect, useState } from "react";
import {
  fetchBlacklist,
  addToBlacklist,
  removeFromBlacklist,
} from "@/api/blacklist";

export type BlacklistEntry = {
  id: number;
  member: {
    id: number;
    username: string;
    email: string;
    nickname: string;
  };
  reason: string;
};

export default function ClientPage() {
  const [blacklist, setBlacklist] = useState<BlacklistEntry[]>([]);
  const [memberId, setMemberId] = useState("");
  const [reason, setReason] = useState("");

  const loadBlacklist = async () => {
    try {
      const data = await fetchBlacklist();
      setBlacklist(data);
    } catch (err) {
      console.error("블랙리스트 조회 실패", err);
    }
  };

  useEffect(() => {
    loadBlacklist();
  }, []);

  const handleAdd = async () => {
    if (!memberId || !reason) return alert("회원 ID와 사유를 입력해주세요");
    try {
      await addToBlacklist(Number(memberId), reason);
      setMemberId("");
      setReason("");
      loadBlacklist();
    } catch (err) {
      console.error("블랙리스트 추가 실패", err);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm("정말 삭제하시겠습니까?")) return;
    try {
      await removeFromBlacklist(id);
      loadBlacklist();
    } catch (err) {
      console.error("삭제 실패", err);
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-8">
      <h1 className="text-2xl font-bold mb-6">🚫 블랙리스트 관리</h1>

      {/* 추가 폼 */}
      <div className="flex gap-4 mb-8">
        <input
          type="number"
          placeholder="회원 ID"
          className="border px-2 py-1 rounded w-32"
          value={memberId}
          onChange={(e) => setMemberId(e.target.value)}
        />
        <input
          placeholder="사유"
          className="border px-2 py-1 rounded flex-1"
          value={reason}
          onChange={(e) => setReason(e.target.value)}
        />
        <button
          className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
          onClick={handleAdd}
        >
          추가
        </button>
      </div>

      {/* 목록 테이블 */}
      {blacklist.length === 0 ? (
        <p>등록된 블랙리스트가 없습니다.</p>
      ) : (
        <table className="w-full border text-center">
          <thead>
            <tr className="bg-gray-100">
              <th>ID</th>
              <th>아이디</th>
              <th>이메일</th>
              <th>닉네임</th>
              <th>사유</th>
              <th>삭제</th>
            </tr>
          </thead>
          <tbody>
            {blacklist.map((entry) => (
              <tr key={entry.id} className="border-t">
                <td>{entry.member.id}</td>
                <td>{entry.member.username}</td>
                <td>{entry.member.email}</td>
                <td>{entry.member.nickname}</td>
                <td>{entry.reason}</td>
                <td>
                  <button
                    className="text-red-500 hover:underline"
                    onClick={() => handleDelete(entry.member.id)}
                  >
                    삭제
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
