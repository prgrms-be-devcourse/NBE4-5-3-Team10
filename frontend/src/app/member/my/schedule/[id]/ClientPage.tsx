"use client";

import { useEffect, useState, ChangeEvent, FormEvent } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";

interface Place {
  id: number;
  placeName: string;
  cityName: string;
}

interface TripInformation {
  tripInformationId: number; // 고유 식별자
  placeId: number;
  cityName: string;
  placeName: string;
  visitTime: string;
  duration: number;
  transportation: string;
  cost: number;
  notes: string;
  //priority: number;
  visited: boolean;
}

interface TripSchedule {
  id: number;
  memberName: string;
  title: string;
  cityName: string;
  description: string;
  startDate: string;
  endDate: string;
  tripInformations?: TripInformation[];
}

interface ApiResponse {
  code: string;
  msg: string;
  data: TripSchedule[];
}

export default function ClientPage() {
  const { id } = useParams();
  const router = useRouter();
  const [schedule, setSchedule] = useState<TripSchedule[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 신규 세부 일정 등록 관련 상태
  const [showNewTripForm, setShowNewTripForm] = useState(false);
  const [newTripInfo, setNewTripInfo] = useState({
    placeId: 0,
    visitTime: "",
    duration: 0,
    transportation: "",
    cost: 0,
    notes: "",
    //priority: 0,
  });
  const [newTripPlaces, setNewTripPlaces] = useState<Place[]>([]);

  // 교통수단 옵션
  const transportationOptions = ["WALK", "BUS", "SUBWAY", "CAR", "TAXI", "ETC"];

  // 여행 일정 조회
  useEffect(() => {
    if (!id) return;
    const token = localStorage.getItem("accessToken");
    if (!token) {
      setError("로그인이 필요합니다.");
      setLoading(false);
      return;
    }
    const fetchSchedule = async () => {
      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/trip/schedule/my-schedules/${id}`,
          {
            method: "GET",
            credentials: "include",
            headers: {
              Authorization: `Bearer ${token}`,
              "Content-Type": "application/json",
            },
          }
        );
        if (!response.ok) {
          throw new Error("일정 정보를 불러오는데 실패했습니다.");
        }
        const result: ApiResponse = await response.json();
        if (!result.data) {
          throw new Error("해당 ID의 여행 일정이 존재하지 않습니다.");
        }
        setSchedule(result.data);
      } catch (err) {
        setError(err instanceof Error ? err.message : "알 수 없는 오류 발생");
      } finally {
        setLoading(false);
      }
    };
    fetchSchedule();
  }, [id]);

  // 신규 세부 일정 등록 시, 해당 여행 일정의 cityName에 맞는 장소 목록 조회
  useEffect(() => {
    // schedule는 배열이지만 조회되는 일정은 하나라고 가정
    if (schedule.length > 0 && schedule[0].cityName) {
      const token = localStorage.getItem("accessToken");
      const fetchPlaces = async () => {
        try {
          const response = await fetch(
            `${
              process.env.NEXT_PUBLIC_API_URL
            }/place?cityName=${encodeURIComponent(schedule[0].cityName)}`,
            {
              method: "GET",
              headers: { "Content-Type": "application/json" },
            }
          );
          if (!response.ok) {
            throw new Error("장소 목록을 불러오는데 실패했습니다.");
          }
          const data = await response.json();
          if (data && data.data) {
            setNewTripPlaces(data.data);
          }
        } catch (err) {
          console.error(err);
        }
      };
      fetchPlaces();
    }
  }, [schedule]);

  // 신규 세부 일정 등록 폼 핸들러
  const handleNewTripInfoChange = (
    e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setNewTripInfo({
      ...newTripInfo,
      [name]:
        name === "duration" || name === "cost" || name === "priority"
          ? Number(value)
          : value,
    });
  };

  const handleRegisterTripInfo = async (e: FormEvent) => {
    e.preventDefault();
    const token = localStorage.getItem("accessToken");
    if (!schedule || schedule.length === 0 || !token) return;
    const payload = {
      tripScheduleId: schedule[0].id,
      placeId: newTripInfo.placeId,
      visitTime: newTripInfo.visitTime,
      duration: newTripInfo.duration,
      transportation: newTripInfo.transportation,
      cost: newTripInfo.cost,
      notes: newTripInfo.notes,
      //priority: newTripInfo.priority,
    };
    try {
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/trip/information`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(payload),
        }
      );
      console.log("세빙일정 등록 : ", payload);
      if (!res.ok) throw new Error("세부 일정 등록 실패");
      alert("세부 일정 등록 성공");
      const result = await res.json();
      // result.data는 새로 등록된 세부 일정
      setSchedule((prev) =>
        prev.map((sch) => ({
          ...sch,
          tripInformations: sch.tripInformations
            ? [...sch.tripInformations, result.data]
            : [result.data],
        }))
      );
      // 초기화
      setNewTripInfo({
        placeId: 0,
        visitTime: "",
        duration: 0,
        transportation: "",
        cost: 0,
        notes: "",
        //priority: 0,
      });
      setShowNewTripForm(false);
    } catch (err) {
      alert(err instanceof Error ? err.message : "알 수 없는 오류");
    }
  };

  // 기존 세부 일정 삭제
  const handleDeleteTripInfo = async (tripInformationId: number) => {
    const token = localStorage.getItem("accessToken");
    try {
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/trip/information/${tripInformationId}`,
        {
          method: "DELETE",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        }
      );
      if (!res.ok) throw new Error("세부 일정 삭제 실패");
      alert("세부 일정 삭제 성공");
      setSchedule((prev) =>
        prev.map((sch) => ({
          ...sch,
          tripInformations: sch.tripInformations?.filter(
            (info) => info.tripInformationId !== tripInformationId
          ),
        }))
      );
    } catch (err) {
      alert(err instanceof Error ? err.message : "알 수 없는 오류");
    }
  };

  // 방문 여부 업데이트 (기존 로직 그대로)
  const updateVisitedStatus = async (
    tripInformationId: number,
    newStatus: boolean
  ) => {
    const token = localStorage.getItem("accessToken");
    try {
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/trip/information/update-visited`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            tripInformationId,
            isVisited: newStatus,
          }),
        }
      );
      if (!res.ok) {
        throw new Error("방문 여부 업데이트에 실패했습니다.");
      }
      setSchedule((prev) =>
        prev.map((sch) => ({
          ...sch,
          tripInformations: sch.tripInformations?.map((info) =>
            info.tripInformationId === tripInformationId
              ? { ...info, visited: newStatus }
              : info
          ),
        }))
      );
    } catch (err) {
      alert(err instanceof Error ? err.message : "알 수 없는 오류");
    }
  };

  if (loading) return <p className="p-6 text-xl text-center">로딩 중...</p>;
  if (error)
    return <p className="p-6 text-xl text-center text-red-600">{error}</p>;
  if (schedule.length === 0)
    return (
      <p className="p-6 text-xl text-center">일정 정보를 찾을 수 없습니다.</p>
    );

  return (
    <div className="max-w-4xl mx-auto p-6">
      <div className="flex justify-between items-center mb-4">
        <button
          className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400 transition"
          onClick={() => router.push("/member/my")}
        >
          ← 뒤로 가기
        </button>
      </div>

      {schedule.map((sch) => (
        <div
          key={sch.id}
          className="mb-8 bg-white rounded-lg overflow-hidden shadow-lg"
        >
          <div className="bg-gradient-to-r from-blue-600 to-blue-400 p-4">
            <h1 className="text-3xl font-bold text-white">{sch.title}</h1>
          </div>
          <div className="p-6">
            <p className="text-lg text-gray-800 mb-1">
              <span className="font-semibold">도시:</span> {sch.cityName}
            </p>
            <p className="text-lg text-gray-800 mb-1">
              <span className="font-semibold">설명:</span> {sch.description}
            </p>
            <p className="text-md text-gray-600 mb-3">
              <span className="font-semibold">여행 기간:</span> {sch.startDate}{" "}
              ~ {sch.endDate}
            </p>
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-2xl font-semibold text-gray-800">
                세부 일정
              </h2>
              <button
                type="button"
                onClick={() => setShowNewTripForm(!showNewTripForm)}
                className="px-4 py-2 bg-green-500 text-white rounded"
              >
                세부 일정 등록
              </button>
            </div>

            {showNewTripForm && (
              <form
                onSubmit={handleRegisterTripInfo}
                className="border p-4 mb-4 rounded"
              >
                <h3 className="text-xl font-semibold mb-2">
                  새 세부 일정 등록
                </h3>
                <div className="mb-2">
                  <label className="block mb-1">장소 선택</label>
                  <select
                    name="placeId"
                    value={newTripInfo.placeId || ""}
                    onChange={handleNewTripInfoChange}
                    className="w-full border rounded p-2"
                    required
                  >
                    <option value="">선택하세요</option>
                    {newTripPlaces.map((place) => (
                      <option key={place.id} value={place.id}>
                        {place.placeName}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="mb-2">
                  <label className="block mb-1">방문 시간</label>
                  <input
                    type="datetime-local"
                    name="visitTime"
                    value={newTripInfo.visitTime}
                    onChange={handleNewTripInfoChange}
                    className="w-full border rounded p-2"
                    required
                  />
                </div>
                <div className="mb-2">
                  <label className="block mb-1">소요 시간 (분)</label>
                  <input
                    type="number"
                    name="duration"
                    value={newTripInfo.duration || ""}
                    onChange={handleNewTripInfoChange}
                    className="w-full border rounded p-2"
                    required
                  />
                </div>
                <div className="mb-2">
                  <label className="block mb-1">교통수단</label>
                  <select
                    name="transportation"
                    value={newTripInfo.transportation}
                    onChange={handleNewTripInfoChange}
                    className="w-full border rounded p-2"
                    required
                  >
                    <option value="">선택하세요</option>
                    {transportationOptions.map((opt) => (
                      <option key={opt} value={opt}>
                        {opt}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="mb-2">
                  <label className="block mb-1">비용</label>
                  <input
                    type="number"
                    name="cost"
                    value={newTripInfo.cost || ""}
                    onChange={handleNewTripInfoChange}
                    className="w-full border rounded p-2"
                  />
                </div>
                <div className="mb-2">
                  <label className="block mb-1">메모</label>
                  <textarea
                    name="notes"
                    value={newTripInfo.notes}
                    onChange={handleNewTripInfoChange}
                    className="w-full border rounded p-2"
                    rows={2}
                  />
                </div>
                {/* <div className="mb-2">
                  <label className="block mb-1">우선순위</label>
                  <input
                    type="number"
                    name="priority"
                    value={newTripInfo.priority || ""}
                    onChange={handleNewTripInfoChange}
                    className="w-full border rounded p-2"
                  />
                </div> */}
                <div className="flex justify-end gap-2">
                  <button
                    type="submit"
                    className="px-4 py-2 bg-green-500 text-white rounded"
                  >
                    등록
                  </button>
                  <button
                    type="button"
                    onClick={() => setShowNewTripForm(false)}
                    className="px-4 py-2 bg-gray-500 text-white rounded"
                  >
                    취소
                  </button>
                </div>
              </form>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {sch.tripInformations && sch.tripInformations.length > 0 ? (
                sch.tripInformations.map((info) => (
                  <div
                    key={info.tripInformationId}
                    className="p-4 bg-gray-50 border border-gray-200 rounded-lg shadow-sm"
                  >
                    <h3 className="text-xl font-bold text-gray-800">
                      {info.placeName}{" "}
                      <span className="text-sm text-gray-500">
                        ({info.cityName})
                      </span>
                    </h3>
                    <p className="text-md text-gray-600">
                      <span className="font-semibold">방문 시간:</span>{" "}
                      {new Date(info.visitTime).toLocaleString()}
                    </p>
                    <p className="text-md text-gray-600">
                      <span className="font-semibold">소요 시간:</span>{" "}
                      {info.duration}분
                    </p>
                    <p className="text-md text-gray-600">
                      <span className="font-semibold">이동 수단:</span>{" "}
                      {info.transportation}
                    </p>
                    <p className="text-md text-gray-600">
                      <span className="font-semibold">비용:</span> {info.cost}원
                    </p>
                    <p className="text-md text-gray-600">
                      <span className="font-semibold">메모:</span> {info.notes}
                    </p>
                    <label className="flex items-center gap-2 mt-2">
                      <input
                        type="checkbox"
                        checked={info.visited}
                        onChange={(e) =>
                          updateVisitedStatus(
                            info.tripInformationId,
                            e.target.checked
                          )
                        }
                      />
                      {info.visited ? "✅ 방문 완료" : "❌ 방문 예정"}
                    </label>
                    <div className="flex gap-2 mt-2">
                      <button
                        onClick={() =>
                          router.push(
                            `/member/my/schedule/update/${info.tripInformationId}`
                          )
                        }
                        className="px-4 py-2 bg-yellow-500 text-white rounded"
                      >
                        수정
                      </button>
                      <button
                        onClick={() =>
                          handleDeleteTripInfo(info.tripInformationId)
                        }
                        className="px-4 py-2 bg-red-500 text-white rounded"
                      >
                        삭제
                      </button>
                    </div>
                  </div>
                ))
              ) : (
                <p className="text-md text-gray-500">
                  세부 일정 정보가 없습니다.
                </p>
              )}
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
