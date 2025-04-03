"use client";

import { useState, useEffect, ChangeEvent, FormEvent } from "react";
import { useParams, useRouter } from "next/navigation";

interface TripInformation {
  tripInformationId: number;
  placeId: number;
  cityName: String;
  placeName: string;
  visitTime: string;
  duration: number;
  transportation: string;
  cost: number;
  notes: string;
  visited: boolean;
}

export default function TripInfoUpdatePage() {
  const { id } = useParams(); // 수정할 세부 일정의 tripInformationId
  const router = useRouter();
  const [tripInfo, setTripInfo] = useState<TripInformation | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    const token = localStorage.getItem("accessToken");
    if (!token) {
      setError("로그인이 필요합니다.");
      setLoading(false);
      return;
    }
    // 세부 일정 정보를 GET으로 호출
    fetch(`${process.env.NEXT_PUBLIC_API_URL}/trip/information/${id}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    })
      .then((res) => res.json())
      .then((result) => {
        console.log("데이터확인 : ", result.data);
        if (result.data) {
          setTripInfo(result.data);
        } else {
          setError("세부 일정 데이터를 찾을 수 없습니다.");
        }
        setLoading(false);
      })
      .catch(() => {
        setError("세부 일정 불러오기에 실패했습니다.");
        setLoading(false);
      });
  }, [id]);

  const handleChange = (
    e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    if (!tripInfo) return;
    setTripInfo({ ...tripInfo, [name]: value });
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!tripInfo) return;
    const token = localStorage.getItem("accessToken");
    if (!token) return;
    // placeName 및 placeId는 변경하지 않으므로 payload에 포함하지 않는다
    const payload = {
      tripInformationId: tripInfo.tripInformationId,
      placeId: tripInfo.placeId,
      cityName: tripInfo.cityName,
      placeName: tripInfo.placeName,
      duration: tripInfo.duration,
      transportation: tripInfo.transportation,
      cost: tripInfo.cost,
      notes: tripInfo.notes,
      visitTime: tripInfo.visitTime,
    };
    try {
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/trip/information/${id}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(payload),
        }
      );
      if (!res.ok) throw new Error("세부 일정 수정 실패");
      alert("세부 일정 수정 성공");
      router.back();
    } catch (err) {
      alert(err instanceof Error ? err.message : "알 수 없는 오류");
    }
  };

  if (loading) return <p>로딩 중...</p>;
  if (error) return <p style={{ color: "red" }}>{error}</p>;
  if (!tripInfo) return <p>데이터가 없습니다.</p>;

  return (
    <form onSubmit={handleSubmit} className="p-6 max-w-2xl mx-auto">
      <h1 className="text-2xl font-bold mb-4">세부 일정 수정</h1>
      <div className="mb-4">
        <p>
          <strong>장소:</strong> {tripInfo.placeName}
        </p>
      </div>
      <div className="mb-4">
        <label className="block mb-2">
          방문 시간:
          <input
            type="datetime-local"
            name="visitTime"
            value={tripInfo.visitTime}
            onChange={handleChange}
            className="border p-2 w-full"
          />
        </label>
      </div>
      <div className="mb-4">
        <label className="block mb-2">
          소요 시간 (분):
          <input
            type="number"
            name="duration"
            value={tripInfo.duration}
            onChange={handleChange}
            className="border p-2 w-full"
          />
        </label>
      </div>
      <div className="mb-4">
        <label className="block mb-2">
          이동 수단:
          <input
            type="text"
            name="transportation"
            value={tripInfo.transportation}
            onChange={handleChange}
            className="border p-2 w-full"
          />
        </label>
      </div>
      <div className="mb-4">
        <label className="block mb-2">
          비용:
          <input
            type="number"
            name="cost"
            value={tripInfo.cost}
            onChange={handleChange}
            className="border p-2 w-full"
          />
        </label>
      </div>
      <div className="mb-4">
        <label className="block mb-2">
          메모:
          <textarea
            name="notes"
            value={tripInfo.notes}
            onChange={handleChange}
            className="border p-2 w-full"
          />
        </label>
      </div>
      <button
        type="submit"
        className="px-4 py-2 bg-blue-500 text-white rounded"
      >
        수정 완료
      </button>
      <button
        type="button"
        onClick={() => router.back()}
        className="px-4 py-2 bg-gray-500 text-white rounded ml-4"
      >
        취소
      </button>
    </form>
  );
}
