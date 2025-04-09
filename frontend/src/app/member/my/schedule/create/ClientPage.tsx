"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";

interface Place {
  id: number;
  placeName: string;
}

interface TripInformation {
  placeId: number;
  visitTime: string;
  duration: number;
  transportation: string;
  cost: number;
  notes: string;
  //priority: number;
}

interface TripSchedule {
  title: string;
  description: string;
  cityName: string;
  startDate: string;
  endDate: string;
  tripInformations: TripInformation[];
}

// 교통수단 옵션 (영문 값)
const transportationOptions = ["WALK", "BUS", "SUBWAY", "CAR", "TAXI", "ETC"];

// 교통수단 한글 매핑 객체 (enum Transportation 참조)
const transportationMapping: Record<string, string> = {
  WALK: "도보",
  BUS: "버스",
  SUBWAY: "기차",
  CAR: "자가용",
  TAXI: "택시",
  ETC: "기타",
};

export default function ClientPage() {
  const router = useRouter();

  // 기본 일정 정보를 위한 상태
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [cityName, setCityName] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  // 동적 세부 일정 정보를 위한 상태
  const [tripInformations, setTripInformations] = useState<TripInformation[]>(
    []
  );

  // 도시 목록 및 장소 목록을 위한 상태
  const [cities, setCities] = useState<string[]>([]);
  const [places, setPlaces] = useState<Place[]>([]);

  // 컴포넌트 마운트 시 중복 제거된 도시 목록을 가져옴
  useEffect(() => {
    const fetchCities = async () => {
      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/place/cities`
        );
        if (!response.ok) {
          throw new Error("도시 목록을 불러오는데 실패했습니다.");
        }
        const data = await response.json();
        if (data && data.data) {
          setCities(data.data);
        }
      } catch (error: any) {
        console.error(error.message);
      }
    };
    fetchCities();
  }, []);

  // cityName이 변경되면 해당 도시에 대한 장소 목록을 가져옴
  useEffect(() => {
    if (!cityName) return;
    const fetchPlaces = async () => {
      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/place?cityName=${cityName}`
        );
        if (!response.ok) {
          throw new Error("장소 목록을 불러오는데 실패했습니다.");
        }
        const data = await response.json();
        if (data && data.data) {
          setPlaces(data.data);
        }
      } catch (error: any) {
        console.error(error.message);
      }
    };
    fetchPlaces();
  }, [cityName]);

  // 새로운 세부 일정을 추가함
  const addTripInformation = () => {
    setTripInformations([
      ...tripInformations,
      {
        placeId: 0,
        visitTime: "",
        duration: 0,
        transportation: "",
        cost: 0,
        notes: "",
        //priority: 0,
      },
    ]);
  };

  // 특정 세부 일정 항목의 필드를 업데이트 함
  const updateTripInformation = (
    index: number,
    field: keyof TripInformation,
    value: string | number | boolean
  ) => {
    const updated = [...tripInformations];
    updated[index] = { ...updated[index], [field]: value };
    setTripInformations(updated);
  };

  // 세부 일정 항목을 제거함
  const removeTripInformation = (index: number) => {
    setTripInformations(tripInformations.filter((_, idx) => idx !== index));
  };

  // 폼 제출을 처리함
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // 전송할 일정 객체를 구성함
    const schedule: TripSchedule = {
      title,
      description,
      cityName,
      startDate,
      endDate,
      tripInformations,
    };

    try {
      const token = localStorage.getItem("accessToken") || "";
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/trip/schedule`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(schedule),
        }
      );
      if (!response.ok) {
        throw new Error("등록에 실패했습니다.");
      }
      router.push("/member/my");
    } catch (error: any) {
      alert(error.message || "알 수 없는 오류가 발생했습니다.");
    }
  };

  return (
    <div className="max-w-3xl mx-auto p-6">
      <button
        className="mb-4 px-4 py-2 bg-gray-300 rounded hover:bg-gray-400 transition"
        onClick={() => router.push("/member/my")}
      >
        ← 뒤로 가기
      </button>
      <h1 className="text-3xl font-bold mb-6">여행 일정 등록</h1>
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* 기본 일정 정보 */}
        <div>
          <label className="block mb-1">제목</label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="w-full border rounded p-2"
            required
          />
        </div>

        <div>
          <label className="block mb-1">설명</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            className="w-full border rounded p-2"
            rows={3}
          />
        </div>

        <div>
          <label className="block mb-1">도시 선택</label>
          <select
            value={cityName}
            onChange={(e) => setCityName(e.target.value)}
            className="w-full border rounded p-2"
            required
          >
            <option value="">선택하세요</option>
            {cities.map((city) => (
              <option key={city} value={city}>
                {city}
              </option>
            ))}
          </select>
        </div>

        <div className="flex space-x-4">
          <div className="flex-1">
            <label className="block mb-1">시작일</label>
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="w-full border rounded p-2"
              required
            />
          </div>
          <div className="flex-1">
            <label className="block mb-1">종료일</label>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="w-full border rounded p-2"
              required
            />
          </div>
        </div>

        {/* 세부 일정 */}
        <div className="border p-4 rounded">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-2xl font-semibold">세부 일정</h2>
            <button
              type="button"
              onClick={addTripInformation}
              className="px-3 py-1 bg-green-500 text-white rounded"
            >
              추가
            </button>
          </div>

          {tripInformations.length === 0 ? (
            <p className="text-gray-600">세부 일정이 없습니다.</p>
          ) : (
            tripInformations.map((info, index) => (
              <div key={index} className="mb-4 border-b pb-4">
                <div className="flex justify-end">
                  <button
                    type="button"
                    onClick={() => removeTripInformation(index)}
                    className="text-red-500 text-sm"
                  >
                    삭제
                  </button>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block mb-1">장소 선택</label>
                    <select
                      value={info.placeId || ""}
                      onChange={(e) =>
                        updateTripInformation(
                          index,
                          "placeId",
                          parseInt(e.target.value, 10) || 0
                        )
                      }
                      className="w-full border rounded p-2"
                      required
                    >
                      <option value="">선택하세요</option>
                      {places.map((place) => (
                        <option key={place.id} value={place.id}>
                          {place.placeName}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block mb-1">방문 시간</label>
                    <input
                      type="datetime-local"
                      value={info.visitTime}
                      onChange={(e) =>
                        updateTripInformation(
                          index,
                          "visitTime",
                          e.target.value
                        )
                      }
                      className="w-full border rounded p-2"
                    />
                  </div>
                  <div>
                    <label className="block mb-1">소요 시간 (분)</label>
                    <input
                      type="number"
                      value={info.duration || ""}
                      onChange={(e) =>
                        updateTripInformation(
                          index,
                          "duration",
                          parseInt(e.target.value, 10) || 0
                        )
                      }
                      className="w-full border rounded p-2"
                    />
                  </div>
                  <div>
                    <label className="block mb-1">교통수단</label>
                    <select
                      value={info.transportation}
                      onChange={(e) =>
                        updateTripInformation(
                          index,
                          "transportation",
                          e.target.value
                        )
                      }
                      className="w-full border rounded p-2"
                    >
                      <option value="">선택하세요</option>
                      {transportationOptions.map((opt) => (
                        <option key={opt} value={opt}>
                          {transportationMapping[opt] || opt}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block mb-1">비용</label>
                    <input
                      type="number"
                      value={info.cost || ""}
                      onChange={(e) =>
                        updateTripInformation(
                          index,
                          "cost",
                          parseInt(e.target.value, 10) || 0
                        )
                      }
                      className="w-full border rounded p-2"
                    />
                  </div>
                  <div>
                    <label className="block mb-1">메모</label>
                    <textarea
                      value={info.notes}
                      onChange={(e) =>
                        updateTripInformation(index, "notes", e.target.value)
                      }
                      className="w-full border rounded p-2"
                      rows={2}
                    />
                  </div>
                  {/* 주석 처리된 우선순위 필드 */}
                </div>
              </div>
            ))
          )}
        </div>

        <button
          type="submit"
          className="w-full bg-blue-500 text-white py-3 rounded text-xl"
        >
          등록하기
        </button>
      </form>
    </div>
  );
}
