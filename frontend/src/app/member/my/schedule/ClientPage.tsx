"use client";

import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation"; // useRouter 추가

interface TripSchedule {
  id: number;
  title: string;
  cityName: string;
  startDate: string;
  endDate: string;
}

const ClientPage = () => {
  const [schedules, setSchedules] = useState<TripSchedule[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter(); // useRouter 사용

  useEffect(() => {
    const fetchSchedules = async () => {
      const token = localStorage.getItem("accessToken");
      if (!token) {
        setError("로그인이 필요합니다.");
        setLoading(false);
        return;
      }

      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/trip/schedule/my-schedules`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        if (!response.ok) {
          const errorResult = await response.json();
          const errorMessage =
            errorResult.msg || "여행 일정 가져오기를 실패했습니다.";
          // 만약 일정이 없다는 에러 메시지라면 빈 배열을 설정합니다.
          if (
            errorMessage === "여행 일정 가져오기를 실패했습니다." ||
            errorMessage === "해당 회원의 여행 일정이 존재하지 않습니다."
          ) {
            setSchedules([]);
          } else {
            throw new Error(errorMessage);
          }
        } else {
          const data = await response.json();
          setSchedules(
            data.data.sort(
              (a: TripSchedule, b: TripSchedule) =>
                new Date(a.startDate).getTime() -
                new Date(b.startDate).getTime()
            )
          );
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : "알 수 없는 오류 발생");
      } finally {
        setLoading(false);
      }
    };

    fetchSchedules();
  }, []);

  // 여행 일정 및 세부일정 삭제
  const handleDelete = async (scheduleId: number) => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      alert("로그인이 필요합니다.");
      return;
    }
    if (!confirm("정말 삭제하시겠습니까?")) return;
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/trip/schedule/my-schedules/${scheduleId}`,
        {
          method: "DELETE",
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      if (!response.ok) {
        const errorResult = await response.json();
        throw new Error(errorResult.msg || "삭제 실패");
      }
      setSchedules(schedules.filter((s) => s.id !== scheduleId));
      alert("일정이 삭제되었습니다.");
    } catch (error: any) {
      alert("삭제 중 오류 발생: " + error.message);
    }
  };

  if (loading) return <p>Loading schedules...</p>;
  if (error) return <p>Error: {error}</p>;

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold">내가 만든 여행</h2>
        <button
          className="bg-green-500 text-white px-3 py-2 rounded"
          onClick={() => router.push("/member/my/schedule/create")}
        >
          여행 일정 등록
        </button>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 p-6">
        {schedules.length > 0 ? (
          schedules.map((schedule) => (
            <div
              key={schedule.id}
              className="bg-white shadow-lg rounded-lg p-6 w-full"
            >
              <h3 className="text-xl font-bold">{schedule.title}</h3>
              <p className="text-md text-gray-600">
                여행지: {schedule.cityName}
              </p>
              <p className="text-md text-gray-600">
                날짜 : {schedule.startDate} ~ {schedule.endDate}
              </p>
              <div className="mt-4">
                <button
                  className="bg-blue-500 text-white px-3 py-2 rounded mr-3"
                  onClick={() =>
                    router.push(`/member/my/schedule/${schedule.id}`)
                  }
                >
                  상세 보기
                </button>
                <button
                  className="bg-gray-500 text-white px-3 py-2 rounded"
                  onClick={() => handleDelete(schedule.id)}
                >
                  일정 삭제
                </button>
              </div>
            </div>
          ))
        ) : (
          <p className="text-lg text-gray-600">등록된 일정이 없습니다.</p>
        )}
      </div>
    </div>
  );
};

export default ClientPage;
