"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { fetchWithAuth } from "@/lib/auth";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
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
import { Checkbox } from "@/components/ui/checkbox";
import { Label } from "@/components/ui/label";
import { Search } from "lucide-react";

const API_BASE_URL = `${process.env.NEXT_PUBLIC_API_URL}/recruits`;
const PLACE_API_URL = `${process.env.NEXT_PUBLIC_API_URL}BLIC_API_BASE_URL}/place`;

interface Place {
  id: number;
  placeName: string;
  cityName: string;
}

export default function CreateRecruitPage() {
  const router = useRouter();
  const [form, setForm] = useState({
    title: "",
    content: "",
    placeId: 0, // 여행지 ID
    startDate: "",
    endDate: "",
    travelStyle: "SIGHTSEEING", // 기본값
    budget: "",
    groupSize: "",
    sameGender: false,
    sameAge: false,
  });
  const [placeQuery, setPlaceQuery] = useState("");
  const [placeResults, setPlaceResults] = useState<Place[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value, type } = e.target;
    const checked =
      type === "checkbox" ? (e.target as HTMLInputElement).checked : undefined;

    setForm((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSelectChange = (name: string, value: string) => {
    setForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleCheckboxChange = (name: string, checked: boolean) => {
    setForm((prev) => ({
      ...prev,
      [name]: checked,
    }));
  };

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

  const handlePlaceSelect = (place: Place) => {
    setForm((prev) => ({ ...prev, placeId: place.id }));
    setPlaceQuery(place.placeName);
    setPlaceResults([]);
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!form.title.trim()) newErrors.title = "제목을 입력해주세요.";
    if (!form.content.trim()) newErrors.content = "내용을 입력해주세요.";
    if (!form.placeId) newErrors.placeId = "여행지를 선택해주세요.";
    if (!form.startDate) newErrors.startDate = "시작일을 선택해주세요.";
    if (!form.endDate) newErrors.endDate = "종료일을 선택해주세요.";
    if (!form.budget) newErrors.budget = "예산을 입력해주세요.";
    if (!form.groupSize) newErrors.groupSize = "모집 인원을 입력해주세요.";

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await fetchWithAuth(API_BASE_URL, {
        method: "POST",
        body: JSON.stringify({
          ...form,
          budget: Number(form.budget),
          groupSize: Number(form.groupSize),
        }),
      });

      if (!response.ok) throw new Error("모집글 작성에 실패했습니다.");

      router.push("/recruit/list");
    } catch (error) {
      console.error("❌ 모집글 작성 오류:", error);
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      {/* 헤더 컴포넌트 사용 */}
      <Header />

      <div className="max-w-4xl mx-auto px-4">
        <h2 className="text-3xl font-bold mb-6">동행 모집 글 작성</h2>

        <Card>
          <CardContent className="p-6">
            <form onSubmit={handleSubmit} className="space-y-6">
              {/* 제목 */}
              <div>
                <Label
                  htmlFor="title"
                  className="block text-sm font-medium text-gray-700 mb-1"
                >
                  제목 <span className="text-red-500">*</span>
                </Label>
                <Input
                  id="title"
                  name="title"
                  value={form.title}
                  onChange={handleChange}
                  placeholder="제목을 입력해주세요"
                  className={errors.title ? "border-red-500" : ""}
                />
                {errors.title && (
                  <p className="mt-1 text-sm text-red-500">{errors.title}</p>
                )}
              </div>

              {/* 내용 */}
              <div>
                <Label
                  htmlFor="content"
                  className="block text-sm font-medium text-gray-700 mb-1"
                >
                  내용 <span className="text-red-500">*</span>
                </Label>
                <Textarea
                  id="content"
                  name="content"
                  value={form.content}
                  onChange={handleChange}
                  placeholder="내용을 입력해주세요"
                  className={`min-h-[150px] ${
                    errors.content ? "border-red-500" : ""
                  }`}
                />
                {errors.content && (
                  <p className="mt-1 text-sm text-red-500">{errors.content}</p>
                )}
              </div>

              {/* 여행지 검색 */}
              <div>
                <Label
                  htmlFor="placeSearch"
                  className="block text-sm font-medium text-gray-700 mb-1"
                >
                  여행지 <span className="text-red-500">*</span>
                </Label>
                <div className="relative">
                  <Input
                    id="placeSearch"
                    value={placeQuery}
                    onChange={handlePlaceSearch}
                    placeholder="여행지를 검색하세요"
                    className={errors.placeId ? "border-red-500" : ""}
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
                {errors.placeId && (
                  <p className="mt-1 text-sm text-red-500">{errors.placeId}</p>
                )}
              </div>

              {/* 여행 날짜 */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <Label
                    htmlFor="startDate"
                    className="block text-sm font-medium text-gray-700 mb-1"
                  >
                    시작일 <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    id="startDate"
                    type="date"
                    name="startDate"
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
                  <Label
                    htmlFor="endDate"
                    className="block text-sm font-medium text-gray-700 mb-1"
                  >
                    종료일 <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    id="endDate"
                    type="date"
                    name="endDate"
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
                <Label
                  htmlFor="travelStyle"
                  className="block text-sm font-medium text-gray-700 mb-1"
                >
                  여행 스타일 <span className="text-red-500">*</span>
                </Label>
                <Select
                  value={form.travelStyle}
                  onValueChange={(value) =>
                    handleSelectChange("travelStyle", value)
                  }
                >
                  <SelectTrigger className="bg-white border border-gray-300">
                    <SelectValue placeholder="여행 스타일 선택" />
                  </SelectTrigger>
                  <SelectContent className="bg-white border border-gray-300">
                    <SelectItem value="SIGHTSEEING">관광</SelectItem>
                    <SelectItem value="RELAXATION">휴양</SelectItem>
                    <SelectItem value="ADVENTURE">액티비티</SelectItem>
                    <SelectItem value="GOURMET">미식</SelectItem>
                    <SelectItem value="SHOPPING">쇼핑</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* 예산 및 모집 인원 */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <Label
                    htmlFor="budget"
                    className="block text-sm font-medium text-gray-700 mb-1"
                  >
                    예산 (원) <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    id="budget"
                    type="number"
                    name="budget"
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
                  <Label
                    htmlFor="groupSize"
                    className="block text-sm font-medium text-gray-700 mb-1"
                  >
                    모집 인원 <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    id="groupSize"
                    type="number"
                    name="groupSize"
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

              {/* 모집 조건 */}
              <div className="space-y-3">
                <Label className="block text-sm font-medium text-gray-700">
                  모집 조건
                </Label>
                <div className="flex items-center space-x-2">
                  <Checkbox
                    id="sameGender"
                    checked={form.sameGender}
                    onCheckedChange={(checked) =>
                      handleCheckboxChange("sameGender", checked as boolean)
                    }
                  />
                  <Label
                    htmlFor="sameGender"
                    className="text-sm font-medium text-gray-700"
                  >
                    동성끼리만 모집
                  </Label>
                </div>
                <div className="flex items-center space-x-2">
                  <Checkbox
                    id="sameAge"
                    checked={form.sameAge}
                    onCheckedChange={(checked) =>
                      handleCheckboxChange("sameAge", checked as boolean)
                    }
                  />
                  <Label
                    htmlFor="sameAge"
                    className="text-sm font-medium text-gray-700"
                  >
                    동일 연령대만 모집
                  </Label>
                </div>
              </div>

              {/* 버튼 */}
              <div className="flex justify-end gap-4">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => router.push("/recruit/list")}
                >
                  취소
                </Button>
                <Button type="submit" disabled={isSubmitting}>
                  {isSubmitting ? "작성 중..." : "작성 완료"}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>

      {/* 푸터 컴포넌트 사용 */}
      <Footer />
    </div>
  );
}

// "use client";

// import { useState } from "react";
// import { useRouter } from "next/navigation";
// import { fetchWithAuth } from "@/lib/auth";
// import Header from "@/components/Header";
// import Footer from "@/components/Footer";

// const API_BASE_URL = `${process.env.NEXT_PUBLIC_API_URL}/recruits`;
// const PLACE_API_URL = `${process.env.NEXT_PUBLIC_API_URL}/place`;

// export default function CreateRecruitPage() {
//   const router = useRouter();
//   const [form, setForm] = useState({
//     title: "",
//     content: "",
//     placeId: 0, // 여행지 ID
//     startDate: "",
//     endDate: "",
//     travelStyle: "SIGHTSEEING", // 기본값
//     budget: "",
//     groupSize: "",
//     sameGender: false,
//     sameAge: false,
//   });
//   const [placeQuery, setPlaceQuery] = useState("");
//   const [placeResults, setPlaceResults] = useState([]);

//   const handleChange = (e) => {
//     const { name, value, type, checked } = e.target;
//     setForm((prev) => ({
//       ...prev,
//       [name]: type === "checkbox" ? checked : value,
//     }));
//   };

//   interface Place {
//     id: number;
//     placeName: string;
//     cityName: string;
//   }

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

//   const handlePlaceSelect = (place: Place) => {
//     setForm((prev) => ({ ...prev, placeId: place.id }));
//     setPlaceQuery(place.placeName);
//     setPlaceResults([]);
//   };

//   const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
//     e.preventDefault();

//     try {
//       const response = await fetchWithAuth(API_BASE_URL, {
//         method: "POST",
//         body: JSON.stringify({
//           ...form,
//           budget: Number(form.budget),
//           groupSize: Number(form.groupSize),
//         }),
//       });

//       if (!response.ok) throw new Error("모집글 작성에 실패했습니다.");

//       router.push("/recruit/list");
//     } catch (error) {
//       console.error("❌ 모집글 작성 오류:", error);
//     }
//   };

//   return (
//     <div className="min-h-screen p-8 bg-gray-50 max-w-xl mx-auto">
//       {/* 헤더 컴포넌트 사용 */}
//       <Header />
//       <h2 className="text-3xl font-bold mb-6">동행 모집 글 작성</h2>
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

//         <div className="relative">
//           <input
//             type="text"
//             placeholder="여행지 검색"
//             value={placeQuery}
//             onChange={handlePlaceSearch}
//             // onFocus={handlePlaceSearch} // ⬅️ 클릭하면 검색 실행
//             className="w-full p-2 border rounded"
//             required
//           />
//           {placeResults.length > 0 && (
//             <ul className="absolute bg-white border mt-1 w-full z-10 max-h-40 overflow-auto">
//               {placeResults.map((place: Place) => (
//                 <li
//                   key={place.id}
//                   onClick={() => handlePlaceSelect(place)}
//                   className="p-2 hover:bg-gray-200 cursor-pointer"
//                 >
//                   {place.placeName} ({place.cityName})
//                 </li>
//               ))}
//             </ul>
//           )}
//         </div>

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
//           value={form.travelStyle}
//           onChange={handleChange}
//           className="w-full p-2 border rounded"
//         >
//           <option value="SIGHTSEEING">관광</option>
//           <option value="RELAXATION">휴양</option>
//           <option value="ADVENTURE">액티비티</option>
//           <option value="GOURMET">미식</option>
//           <option value="SHOPPING">쇼핑</option>
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
//         <label className="flex items-center space-x-2">
//           <input
//             type="checkbox"
//             name="sameGender"
//             checked={form.sameGender}
//             onChange={handleChange}
//           />
//           <span>동성끼리만 모집</span>
//         </label>
//         <label className="flex items-center space-x-2">
//           <input
//             type="checkbox"
//             name="sameAge"
//             checked={form.sameAge}
//             onChange={handleChange}
//           />
//           <span>동일 연령대만 모집</span>
//         </label>
//         <button
//           type="submit"
//           className="w-full p-2 bg-blue-600 text-white rounded hover:bg-blue-700"
//         >
//           작성 완료
//         </button>
//       </form>
//       {/* 푸터 컴포넌트 사용 */}
//       <Footer />
//     </div>
//   );
// }
