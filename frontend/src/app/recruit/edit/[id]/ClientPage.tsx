"use client";

import { useEffect, useState } from "react";
import { useRouter, useParams } from "next/navigation";
import { fetchWithAuth } from "@/lib/auth";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import { AlertCircle, Search } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";

const API_BASE_URL = `${process.env.NEXT_PUBLIC_API_URL}/recruits`;
const PLACE_API_URL = `${process.env.NEXT_PUBLIC_API_URL}/place`;

// 여행 스타일 (한글 ↔ 영문 변환)
const travelStyleMap = {
  SIGHTSEEING: "관광",
  RELAXATION: "휴양",
  ADVENTURE: "액티비티",
  GOURMET: "미식",
  SHOPPING: "쇼핑",
};

interface Place {
  id: number;
  placeName: string;
  cityName: string;
}

export default function EditRecruitPage() {
  const router = useRouter();
  const params = useParams();
  const recruitId = params.id;

  const [form, setForm] = useState({
    title: "",
    content: "",
    placeId: 0,
    placeCityName: "",
    placePlaceName: "",
    startDate: "",
    endDate: "",
    travelStyle: "SIGHTSEEING",
    budget: 0,
    groupSize: 2,
    isClosed: false,
    sameGender: false,
    sameAge: false,
  });

  const [loading, setLoading] = useState(true);
  const [formError, setFormError] = useState<string | null>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [placeQuery, setPlaceQuery] = useState("");
  const [placeResults, setPlaceResults] = useState<Place[]>([]);

  // 기존 데이터를 가져온 후, placeQuery를 placePlaceName으로 설정
  useEffect(() => {
    if (form.placePlaceName) {
      setPlaceQuery(form.placePlaceName);
    }
  }, [form.placePlaceName]);

  // 기존 데이터를 API에서 가져오기
  useEffect(() => {
    async function fetchRecruit() {
      try {
        const response = await fetchWithAuth(`${API_BASE_URL}/${recruitId}`);
        if (!response.ok) throw new Error("기존 모집글을 불러오지 못했습니다.");
        const data = await response.json();

        // 데이터 변환 (한글 ↔ 영문 여행 스타일)
        setForm({
          title: data.data.title,
          content: data.data.content,
          placeId: data.data.placeId,
          placeCityName: data.data.placeCityName,
          placePlaceName: data.data.placePlaceName,
          startDate: data.data.startDate,
          endDate: data.data.endDate,
          travelStyle:
            Object.keys(travelStyleMap).find(
              (key) => travelStyleMap[key] === data.data.travelStyle
            ) || "SIGHTSEEING",
          budget: data.data.budget,
          groupSize: data.data.groupSize,
          isClosed: data.data.isClosed ?? false, // undefined 방지
          sameGender: data.data.sameGender ?? false, // undefined 방지
          sameAge: data.data.sameAge ?? false, // undefined 방지
        });

        setLoading(false);
      } catch (error) {
        console.error("❌ 기존 모집글 불러오기 오류:", error);
        setFormError("기존 모집글을 불러오는 중 오류가 발생했습니다.");
        setLoading(false);
      }
    }

    fetchRecruit();
  }, [recruitId]);

  // 여행지 검색
  const handlePlaceSearch = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const query = e.target.value;
    setPlaceQuery(query);
    if (!query) return setPlaceResults([]);

    try {
      const response = await fetch(`${PLACE_API_URL}`);
      if (!response.ok) throw new Error("장소 목록을 불러올 수 없습니다.");
      const data = await response.json();
      const filteredPlaces = data.data.filter((place: Place) =>
        place.placeName.toLowerCase().includes(query.toLowerCase())
      );
      setPlaceResults(filteredPlaces);
    } catch (error) {
      console.error("❌ 장소 검색 오류:", error);
    }
  };

  // 여행지 선택
  const handlePlaceSelect = (place: Place) => {
    setForm((prev) => ({
      ...prev,
      placeId: place.id,
      placeCityName: place.cityName,
      placePlaceName: place.placeName,
    }));
    setPlaceQuery(place.placeName);
    setPlaceResults([]);
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;

    setForm((prev) => {
      let newValue;
      if (name === "travelStyle") {
        // 한글 값을 영어 Enum 값으로 변환
        newValue =
          Object.keys(travelStyleMap).find(
            (key) => travelStyleMap[key] === value
          ) || "SIGHTSEEING";
      } else {
        newValue = type === "checkbox" ? checked : value;
      }

      // 에러 상태 초기화
      if (errors[name]) {
        setErrors({ ...errors, [name]: "" });
      }

      console.log(`🔄 변경됨: ${name} =`, newValue);
      return { ...prev, [name]: newValue };
    });
  };

  const handleTravelStyleChange = (value) => {
    setForm((prev) => ({
      ...prev,
      travelStyle:
        Object.keys(travelStyleMap).find(
          (key) => travelStyleMap[key] === value
        ) || "SIGHTSEEING",
    }));

    // 에러 상태 초기화
    if (errors.travelStyle) {
      setErrors({ ...errors, travelStyle: "" });
    }
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!form.title.trim()) {
      newErrors.title = "제목을 입력해주세요.";
    }

    if (!form.content.trim()) {
      newErrors.content = "내용을 입력해주세요.";
    }

    if (!form.startDate) {
      newErrors.startDate = "시작 날짜를 선택해주세요.";
    }

    if (!form.endDate) {
      newErrors.endDate = "종료 날짜를 선택해주세요.";
    }

    if (form.budget <= 0) {
      newErrors.budget = "예산을 입력해주세요.";
    }

    if (form.groupSize < 2) {
      newErrors.groupSize = "모집 인원은 최소 2명 이상이어야 합니다.";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    console.log("🚀 전송할 데이터:", JSON.stringify(form, null, 2));

    try {
      const response = await fetchWithAuth(`${API_BASE_URL}/${recruitId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          placeId: form.placeId,
          title: form.title,
          content: form.content,
          isClosed: form.isClosed,
          startDate: form.startDate,
          endDate: form.endDate,
          travelStyle: form.travelStyle,
          sameGender: form.sameGender,
          sameAge: form.sameAge,
          budget: Number(form.budget),
          groupSize: Number(form.groupSize),
        }),
      });

      if (!response.ok) throw new Error("모집글 수정에 실패했습니다.");

      router.push(`/recruit/${recruitId}`);
    } catch (error) {
      console.error("❌ 모집글 수정 오류:", error);
      setFormError("모집글 수정 중 오류가 발생했습니다.");
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex justify-center items-center">
        <p className="text-lg">로딩 중...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <div className="max-w-3xl mx-auto px-4 py-8">
        <h2 className="text-3xl font-bold mb-6">동행 모집 글 수정</h2>

        <Card>
          <CardContent className="p-6">
            {formError && (
              <Alert variant="destructive" className="mb-6">
                <AlertCircle className="h-4 w-4" />
                <AlertTitle>오류</AlertTitle>
                <AlertDescription>{formError}</AlertDescription>
              </Alert>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
              {/* 제목 */}
              <div>
                <label
                  htmlFor="title"
                  className="block text-sm font-medium text-gray-700 mb-1"
                >
                  제목 <span className="text-red-500">*</span>
                </label>
                <Input
                  id="title"
                  name="title"
                  value={form.title}
                  onChange={handleChange}
                  placeholder="동행 모집 제목을 입력해주세요"
                  className={errors.title ? "border-red-500" : ""}
                />
                {errors.title && (
                  <p className="mt-1 text-sm text-red-500">{errors.title}</p>
                )}
              </div>

              {/* 내용 */}
              <div>
                <label
                  htmlFor="content"
                  className="block text-sm font-medium text-gray-700 mb-1"
                >
                  내용 <span className="text-red-500">*</span>
                </label>
                <Textarea
                  id="content"
                  name="content"
                  value={form.content}
                  onChange={handleChange}
                  placeholder="동행 모집 내용을 자세히 작성해주세요"
                  className={`min-h-[200px] ${
                    errors.content ? "border-red-500" : ""
                  }`}
                />
                {errors.content && (
                  <p className="mt-1 text-sm text-red-500">{errors.content}</p>
                )}
              </div>

              {/* 여행지 검색 추가 */}
              <div>
                <label
                  htmlFor="placeSearch"
                  className="block text-sm font-medium text-gray-700 mb-1"
                >
                  여행지 <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <Input
                    id="placeSearch"
                    value={placeQuery}
                    onChange={handlePlaceSearch}
                    placeholder="여행지를 검색하세요"
                    className="w-full"
                  />
                  <Search className="absolute right-3 top-2.5 h-4 w-4 text-gray-400" />

                  {placeResults.length > 0 && (
                    <ul className="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-md shadow-lg max-h-60 overflow-auto">
                      {placeResults.map((place) => (
                        <li
                          key={place.id}
                          onClick={() => handlePlaceSelect(place)}
                          className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                        >
                          {place.placeName} ({place.cityName})
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              </div>

              {/* 날짜 */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label
                    htmlFor="startDate"
                    className="block text-sm font-medium text-gray-700 mb-1"
                  >
                    시작 날짜 <span className="text-red-500">*</span>
                  </label>
                  <Input
                    id="startDate"
                    name="startDate"
                    type="date"
                    value={form.startDate}
                    onChange={handleChange}
                    className={errors.startDate ? "border-red-500" : ""}
                  />
                  {errors.startDate && (
                    <p className="mt-1 text-sm text-red-500">
                      {errors.startDate}
                    </p>
                  )}
                </div>

                <div>
                  <label
                    htmlFor="endDate"
                    className="block text-sm font-medium text-gray-700 mb-1"
                  >
                    종료 날짜 <span className="text-red-500">*</span>
                  </label>
                  <Input
                    id="endDate"
                    name="endDate"
                    type="date"
                    value={form.endDate}
                    onChange={handleChange}
                    className={errors.endDate ? "border-red-500" : ""}
                  />
                  {errors.endDate && (
                    <p className="mt-1 text-sm text-red-500">
                      {errors.endDate}
                    </p>
                  )}
                </div>
              </div>

              {/* 여행 스타일 */}
              <div>
                <label
                  htmlFor="travelStyle"
                  className="block text-sm font-medium text-gray-700 mb-1"
                >
                  여행 스타일 <span className="text-red-500">*</span>
                </label>
                <Select
                  value={travelStyleMap[form.travelStyle]}
                  onValueChange={handleTravelStyleChange}
                >
                  <SelectTrigger
                    className={`bg-white border border-gray-300 ${
                      errors.travelStyle ? "border-red-500" : ""
                    }`}
                  >
                    <SelectValue placeholder="여행 스타일을 선택해주세요" />
                  </SelectTrigger>
                  <SelectContent className="bg-white shadow-md border border-gray-300">
                    {Object.entries(travelStyleMap).map(([key, label]) => (
                      <SelectItem key={key} value={label}>
                        {label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {errors.travelStyle && (
                  <p className="mt-1 text-sm text-red-500">
                    {errors.travelStyle}
                  </p>
                )}
              </div>

              {/* 예산 및 인원 */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label
                    htmlFor="budget"
                    className="block text-sm font-medium text-gray-700 mb-1"
                  >
                    예산 (원) <span className="text-red-500">*</span>
                  </label>
                  <Input
                    id="budget"
                    name="budget"
                    type="number"
                    value={form.budget}
                    onChange={handleChange}
                    placeholder="예산을 입력해주세요"
                    className={errors.budget ? "border-red-500" : ""}
                  />
                  {errors.budget && (
                    <p className="mt-1 text-sm text-red-500">{errors.budget}</p>
                  )}
                </div>

                <div>
                  <label
                    htmlFor="groupSize"
                    className="block text-sm font-medium text-gray-700 mb-1"
                  >
                    모집 인원 <span className="text-red-500">*</span>
                  </label>
                  <Input
                    id="groupSize"
                    name="groupSize"
                    type="number"
                    value={form.groupSize}
                    onChange={handleChange}
                    placeholder="모집 인원을 입력해주세요"
                    className={errors.groupSize ? "border-red-500" : ""}
                  />
                  {errors.groupSize && (
                    <p className="mt-1 text-sm text-red-500">
                      {errors.groupSize}
                    </p>
                  )}
                </div>
              </div>

              {/* 체크박스 옵션들 */}
              <div className="space-y-3">
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    id="isClosed"
                    name="isClosed"
                    checked={form.isClosed}
                    onChange={handleChange}
                    className="h-4 w-4 text-blue-600 rounded border-gray-300"
                  />
                  <label
                    htmlFor="isClosed"
                    className="ml-2 text-sm font-medium text-gray-700"
                  >
                    모집 마감
                  </label>
                </div>

                <div className="flex items-center">
                  <input
                    type="checkbox"
                    id="sameGender"
                    name="sameGender"
                    checked={form.sameGender}
                    onChange={handleChange}
                    className="h-4 w-4 text-blue-600 rounded border-gray-300"
                  />
                  <label
                    htmlFor="sameGender"
                    className="ml-2 text-sm font-medium text-gray-700"
                  >
                    동일 성별만 모집
                  </label>
                </div>

                <div className="flex items-center">
                  <input
                    type="checkbox"
                    id="sameAge"
                    name="sameAge"
                    checked={form.sameAge}
                    onChange={handleChange}
                    className="h-4 w-4 text-blue-600 rounded border-gray-300"
                  />
                  <label
                    htmlFor="sameAge"
                    className="ml-2 text-sm font-medium text-gray-700"
                  >
                    동일 연령대만 모집
                  </label>
                </div>
              </div>

              {/* 버튼들 */}
              <div className="flex justify-end gap-4 mt-6">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => router.push(`/recruit/${recruitId}`)}
                >
                  취소
                </Button>
                <Button type="submit">수정 완료</Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>

      <Footer />
    </div>
  );
}

// "use client";

// import { useEffect, useState } from "react";
// import { useRouter, useParams } from "next/navigation";
// import { fetchWithAuth } from "@/lib/auth";
// import Header from "@/components/Header";
// import Footer from "@/components/Footer";
// import { AlertCircle, Search } from "lucide-react";
// import { Button } from "@/components/ui/button";
// import { Card, CardContent } from "@/components/ui/card";
// import { Input } from "@/components/ui/input";
// import { Textarea } from "@/components/ui/textarea";
// import {
//   Select,
//   SelectContent,
//   SelectItem,
//   SelectTrigger,
//   SelectValue,
// } from "@/components/ui/select";
// import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";

// const API_BASE_URL = `${process.env.NEXT_PUBLIC_API_URL}/recruits`;
// const PLACE_API_URL = `${process.env.NEXT_PUBLIC_API_URL}/place`;

// // 여행 스타일 (한글 ↔ 영문 변환)
// const travelStyleMap = {
//   SIGHTSEEING: "관광",
//   RELAXATION: "휴양",
//   ADVENTURE: "액티비티",
//   GOURMET: "미식",
//   SHOPPING: "쇼핑",
// };

// interface Place {
//   id: number;
//   placeName: string;
//   cityName: string;
// }

// export default function EditRecruitPage() {
//   const router = useRouter();
//   const params = useParams();
//   const recruitId = params.id;

//   const [form, setForm] = useState({
//     title: "",
//     content: "",
//     placeId: 0,
//     placeCityName: "",
//     placePlaceName: "",
//     startDate: "",
//     endDate: "",
//     travelStyle: "SIGHTSEEING",
//     budget: 0,
//     groupSize: 2,
//     isClosed: false,
//     sameGender: false,
//     sameAge: false,
//   });

//   const [placeQuery, setPlaceQuery] = useState("");
//   const [placeResults, setPlaceResults] = useState<Place[]>([]);
//   const [loading, setLoading] = useState(true);
//   const [formError, setFormError] = useState<string | null>(null);
//   const [errors, setErrors] = useState<Record<string, string>>({});

//   // 기존 데이터를 API에서 가져오기
//   useEffect(() => {
//     async function fetchRecruit() {
//       try {
//         const response = await fetchWithAuth(`${API_BASE_URL}/${recruitId}`);
//         if (!response.ok) throw new Error("기존 모집글을 불러오지 못했습니다.");
//         const data = await response.json();

//         setForm({
//           title: data.data.title,
//           content: data.data.content,
//           placeId: data.data.placeId,
//           placeCityName: data.data.placeCityName,
//           placePlaceName: data.data.placePlaceName,
//           startDate: data.data.startDate,
//           endDate: data.data.endDate,
//           travelStyle:
//             Object.keys(travelStyleMap).find(
//               (key) => travelStyleMap[key] === data.data.travelStyle
//             ) || "SIGHTSEEING",
//           budget: data.data.budget,
//           groupSize: data.data.groupSize,
//           isClosed: data.data.isClosed ?? false,
//           sameGender: data.data.sameGender ?? false,
//           sameAge: data.data.sameAge ?? false,
//         });

//         setLoading(false);
//       } catch (error) {
//         console.error("❌ 기존 모집글 불러오기 오류:", error);
//         setFormError("기존 모집글을 불러오는 중 오류가 발생했습니다.");
//         setLoading(false);
//       }
//     }

//     fetchRecruit();
//   }, [recruitId]);

//   // 여행지 검색
//   const handlePlaceSearch = async (e: React.ChangeEvent<HTMLInputElement>) => {
//     const query = e.target.value;
//     setPlaceQuery(query);
//     if (!query) return setPlaceResults([]);

//     try {
//       const response = await fetch(`${PLACE_API_URL}`);
//       if (!response.ok) throw new Error("장소 목록을 불러올 수 없습니다.");
//       const data = await response.json();
//       const filteredPlaces = data.data.filter((place: Place) =>
//         place.placeName.toLowerCase().includes(query.toLowerCase())
//       );
//       setPlaceResults(filteredPlaces);
//     } catch (error) {
//       console.error("❌ 장소 검색 오류:", error);
//     }
//   };

//   // 여행지 선택
//   const handlePlaceSelect = (place: Place) => {
//     setForm((prev) => ({
//       ...prev,
//       placeId: place.id,
//       placeCityName: place.cityName,
//       placePlaceName: place.placeName,
//     }));
//     setPlaceQuery(place.placeName);
//     setPlaceResults([]);
//   };

//   const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
//     e.preventDefault();

//     try {
//       const response = await fetchWithAuth(`${API_BASE_URL}/${recruitId}`, {
//         method: "PUT",
//         headers: { "Content-Type": "application/json" },
//         body: JSON.stringify({
//           ...form,
//           placeId: form.placeId, // 변경된 장소 반영
//           budget: Number(form.budget),
//           groupSize: Number(form.groupSize),
//         }),
//       });

//       if (!response.ok) throw new Error("모집글 수정에 실패했습니다.");

//       router.push(`/recruit/${recruitId}`);
//     } catch (error) {
//       console.error("❌ 모집글 수정 오류:", error);
//       setFormError("모집글 수정 중 오류가 발생했습니다.");
//     }
//   };

//   if (loading) {
//     return (
//       <div className="min-h-screen flex justify-center items-center">
//         <p className="text-lg">로딩 중...</p>
//       </div>
//     );
//   }

//   return (
//     <div className="min-h-screen bg-gray-50">
//       <Header />
//       <div className="max-w-3xl mx-auto px-4 py-8">
//         <h2 className="text-3xl font-bold mb-6">동행 모집 글 수정</h2>

//         <Card>
//           <CardContent className="p-6">
//             {formError && (
//               <Alert variant="destructive" className="mb-6">
//                 <AlertCircle className="h-4 w-4" />
//                 <AlertTitle>오류</AlertTitle>
//                 <AlertDescription>{formError}</AlertDescription>
//               </Alert>
//             )}

//             <form onSubmit={handleSubmit} className="space-y-6">
//               {/* 장소 검색 추가 */}
//               <div>
//                 <label
//                   htmlFor="placeSearch"
//                   className="block text-sm font-medium text-gray-700 mb-1"
//                 >
//                   여행지 <span className="text-red-500">*</span>
//                 </label>
//                 <div className="relative">
//                   <Input
//                     id="placeSearch"
//                     value={placeQuery}
//                     onChange={handlePlaceSearch}
//                     placeholder="여행지를 검색하세요"
//                     className="w-full"
//                   />
//                   <Search className="absolute right-3 top-2.5 h-4 w-4 text-gray-400" />

//                   {placeResults.length > 0 && (
//                     <ul className="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-md shadow-lg max-h-60 overflow-auto">
//                       {placeResults.map((place) => (
//                         <li
//                           key={place.id}
//                           onClick={() => handlePlaceSelect(place)}
//                           className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
//                         >
//                           {place.placeName} ({place.cityName})
//                         </li>
//                       ))}
//                     </ul>
//                   )}
//                 </div>
//               </div>

//               <Button type="submit">수정 완료</Button>
//             </form>
//           </CardContent>
//         </Card>
//       </div>

//       <Footer />
//     </div>
//   );
// }

/////////////////////////////////////
// "use client";

// import { useEffect, useState } from "react";
// import { useRouter, useParams } from "next/navigation";
// import { fetchWithAuth } from "@/lib/auth";
// import Header from "@/components/Header";
// import Footer from "@/components/Footer";

// const API_BASE_URL = `${process.env.NEXT_PUBLIC_API_URL}/recruits`;

// // ✅ 여행 스타일 (한글 ↔ 영문 변환)
// const travelStyleMap = {
//   SIGHTSEEING: "관광",
//   RELAXATION: "휴양",
//   ADVENTURE: "액티비티",
//   GOURMET: "미식",
//   SHOPPING: "쇼핑",
// };

// export default function EditRecruitPage() {
//   const router = useRouter();
//   const params = useParams();
//   const recruitId = params.id;

//   const [form, setForm] = useState({
//     title: "",
//     content: "",
//     placeId: 0,
//     placeCityName: "",
//     placePlaceName: "",
//     startDate: "",
//     endDate: "",
//     travelStyle: "SIGHTSEEING",
//     budget: 0,
//     groupSize: 2,
//     isClosed: false,
//     sameGender: false,
//     sameAge: false,
//   });

//   const [loading, setLoading] = useState(true);

//   // ✅ 기존 데이터를 API에서 가져오기
//   useEffect(() => {
//     async function fetchRecruit() {
//       try {
//         const response = await fetchWithAuth(`${API_BASE_URL}/${recruitId}`);
//         if (!response.ok) throw new Error("기존 모집글을 불러오지 못했습니다.");
//         const data = await response.json();

//         // ✅ 데이터 변환 (한글 ↔ 영문 여행 스타일)
//         setForm({
//           title: data.data.title,
//           content: data.data.content,
//           placeId: data.data.placeId,
//           placeCityName: data.data.placeCityName,
//           placePlaceName: data.data.placePlaceName,
//           startDate: data.data.startDate,
//           endDate: data.data.endDate,
//           travelStyle:
//             Object.keys(travelStyleMap).find(
//               (key) => travelStyleMap[key] === data.data.travelStyle
//             ) || "SIGHTSEEING",
//           budget: data.data.budget,
//           groupSize: data.data.groupSize,
//           isClosed: data.data.isClosed ?? false, // ✅ undefined 방지
//           sameGender: data.data.sameGender ?? false, // ✅ undefined 방지
//           sameAge: data.data.sameAge ?? false, // ✅ undefined 방지
//         });

//         setLoading(false);
//       } catch (error) {
//         console.error("❌ 기존 모집글 불러오기 오류:", error);
//         setLoading(false);
//       }
//     }

//     fetchRecruit();
//   }, [recruitId]);

//   // const handleChange = (e) => {
//   //   const { name, value, type, checked } = e.target;
//   //   setForm((prev) => ({
//   //     ...prev,
//   //     [name]:
//   //       name === "travelStyle"
//   //         ? Object.keys(travelStyleMap).find(
//   //             (key) => travelStyleMap[key] === value
//   //           ) || "SIGHTSEEING"
//   //         : type === "checkbox"
//   //         ? checked
//   //         : value,
//   //   }));
//   // };

//   // const handleChange = (e) => {
//   //   const { name, value, type, checked } = e.target;
//   //   setForm((prev) => {
//   //     const newValue = type === "checkbox" ? checked : value;
//   //     console.log(`🔄 변경됨: ${name} =`, newValue); // ✅ 값이 정상적으로 변경되는지 확인
//   //     return { ...prev, [name]: newValue };
//   //   });
//   // };

//   // const handleChange = (e) => {
//   //   console.log("🟢 체크박스 변경 감지됨!", e.target.name, e.target.checked);

//   //   const { name, value, type, checked } = e.target;
//   //   setForm((prev) => {
//   //     const newValue = type === "checkbox" ? checked : value;
//   //     console.log(`🔄 변경됨: ${name} =`, newValue); // ✅ 값이 정상적으로 변경되는지 확인
//   //     return { ...prev, [name]: newValue };
//   //   });
//   // };

//   const handleChange = (e) => {
//     const { name, value, type, checked } = e.target;

//     setForm((prev) => {
//       let newValue;
//       if (name === "travelStyle") {
//         // 한글 값을 영어 Enum 값으로 변환
//         newValue =
//           Object.keys(travelStyleMap).find(
//             (key) => travelStyleMap[key] === value
//           ) || "SIGHTSEEING";
//       } else {
//         newValue = type === "checkbox" ? checked : value;
//       }

//       console.log(`🔄 변경됨: ${name} =`, newValue); // ✅ 디버깅용 로그
//       return { ...prev, [name]: newValue };
//     });
//   };

//   const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
//     e.preventDefault();

//     console.log("🚀 전송할 데이터:", JSON.stringify(form, null, 2)); // ✅ 실제 요청 데이터 확인

//     try {
//       const response = await fetchWithAuth(`${API_BASE_URL}/${recruitId}`, {
//         method: "PUT",
//         headers: { "Content-Type": "application/json" },
//         body: JSON.stringify({
//           placeId: form.placeId,
//           title: form.title,
//           content: form.content,
//           isClosed: form.isClosed,
//           startDate: form.startDate,
//           endDate: form.endDate,
//           travelStyle: form.travelStyle,
//           sameGender: form.sameGender,
//           sameAge: form.sameAge,
//           budget: Number(form.budget),
//           groupSize: Number(form.groupSize),
//         }),
//       });

//       if (!response.ok) throw new Error("모집글 수정에 실패했습니다.");

//       router.push(`/recruit/${recruitId}`);
//     } catch (error) {
//       console.error("❌ 모집글 수정 오류:", error);
//     }
//   };

//   if (loading) return <p>로딩 중...</p>;

//   return (
//     <div className="min-h-screen p-8 bg-gray-50 max-w-xl mx-auto">
//       <Header />
//       <h2 className="text-3xl font-bold mb-6">동행 모집 글 수정</h2>
//       <form onSubmit={handleSubmit} className="space-y-4">
//         <input
//           type="text"
//           name="title"
//           placeholder="제목"
//           value={form.title}
//           onChange={handleChange}
//           className="w-full p-2 border rounded"
//           required
//         />
//         <textarea
//           name="content"
//           placeholder="내용"
//           value={form.content}
//           onChange={handleChange}
//           className="w-full p-2 border rounded"
//           required
//         />
//         <p className="text-gray-600">
//           📍 여행지: {form.placeCityName}, {form.placePlaceName}
//         </p>
//         <input
//           type="date"
//           name="startDate"
//           value={form.startDate}
//           onChange={handleChange}
//           className="w-full p-2 border rounded"
//           required
//         />
//         <input
//           type="date"
//           name="endDate"
//           value={form.endDate}
//           onChange={handleChange}
//           className="w-full p-2 border rounded"
//           required
//         />
//         <select
//           name="travelStyle"
//           value={travelStyleMap[form.travelStyle]}
//           onChange={handleChange}
//           className="w-full p-2 border rounded"
//         >
//           {Object.entries(travelStyleMap).map(([key, label]) => (
//             <option key={key} value={label}>
//               {label}
//             </option>
//           ))}
//         </select>
//         <input
//           type="number"
//           name="budget"
//           placeholder="예산 (원)"
//           value={form.budget}
//           onChange={handleChange}
//           className="w-full p-2 border rounded"
//           required
//         />
//         <input
//           type="number"
//           name="groupSize"
//           placeholder="모집 인원"
//           value={form.groupSize}
//           onChange={handleChange}
//           className="w-full p-2 border rounded"
//           required
//         />
//         <label>
//           <input
//             type="checkbox"
//             name="isClosed"
//             checked={form.isClosed}
//             onChange={handleChange}
//           />
//           모집 마감
//         </label>
//         <label>
//           <input
//             type="checkbox"
//             name="sameGender"
//             checked={form.sameGender}
//             onChange={handleChange}
//           />
//           동성끼리만 모집
//         </label>
//         <label>
//           <input
//             type="checkbox"
//             name="sameAge"
//             checked={form.sameAge}
//             onChange={handleChange}
//           />
//           동일 연령대만 모집
//         </label>
//         <button
//           type="submit"
//           className="w-full p-2 bg-blue-600 text-white rounded hover:bg-blue-700"
//         >
//           수정 완료
//         </button>
//       </form>
//       <Footer />
//     </div>
//   );
// }
