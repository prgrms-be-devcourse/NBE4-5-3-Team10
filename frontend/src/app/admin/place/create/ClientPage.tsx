"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { Upload, X } from "lucide-react";

export default function ClientPage() {
  const router = useRouter();
  const [cityName, setCityName] = useState("");
  const [placeName, setPlaceName] = useState("");
  const [description, setDescription] = useState("");
  const [category, setCategory] = useState("");
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [previewImage, setPreviewImage] = useState<string | null>(null);
  const [error, setError] = useState("");

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      setImageFile(file);
      const preview = URL.createObjectURL(file);
      setPreviewImage(preview);
    }
  };

  const handleRemoveImage = () => {
    if (previewImage) {
      URL.revokeObjectURL(previewImage);
    }
    setImageFile(null);
    setPreviewImage(null);
  };

  useEffect(() => {
    return () => {
      if (previewImage) {
        URL.revokeObjectURL(previewImage);
      }
    };
  }, [previewImage]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const formData = new FormData();
    formData.append("cityName", cityName);
    formData.append("placeName", placeName);
    formData.append("description", description);
    formData.append("category", category);
    if (imageFile) {
      formData.append("imageUrl", imageFile);
    }

    // FormData 내용 확인: 각 key, value 쌍 출력
    for (let [key, value] of formData.entries()) {
      console.log("키,값 : ", key, value);
    }

    try {
      // 관리자 토큰을 헤더에 포함하여 요청
      const token = localStorage.getItem("accessToken");
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/place`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
        body: formData,
      });

      if (!res.ok) {
        throw new Error("등록에 실패하였습니다.");
      }
      router.push("/admin/place");
    } catch (err) {
      console.error(err);
      setError("여행지 등록 중 오류가 발생했습니다. 다시 시도해주세요.");
    }
  };

  return (
    <div className="max-w-xl mx-auto p-4">
      <button className="mb-4 text-blue-500" onClick={() => router.back()}>
        뒤로가기
      </button>
      <h2 className="text-2xl font-bold mb-4">여행지 등록</h2>
      {error && <p className="text-red-500 mb-4">{error}</p>}
      <form onSubmit={handleSubmit} encType="multipart/form-data">
        <div className="mb-4">
          <label className="block text-gray-700">도시 이름</label>
          <input
            type="text"
            value={cityName}
            onChange={(e) => setCityName(e.target.value)}
            className="w-full p-2 border rounded-md"
            required
          />
        </div>
        <div className="mb-4">
          <label className="block text-gray-700">여행지 이름</label>
          <input
            type="text"
            value={placeName}
            onChange={(e) => setPlaceName(e.target.value)}
            className="w-full p-2 border rounded-md"
            required
          />
        </div>
        <div className="mb-4">
          <label className="block text-gray-700">설명</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            className="w-full p-2 border rounded-md"
            rows={4}
            required
          />
        </div>
        <div className="mb-4">
          <label className="block text-gray-700">카테고리</label>
          <select
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            className="w-full p-2 border rounded-md"
            required
          >
            <option value="">카테고리 선택</option>
            <option value="PLACE">장소</option>
            <option value="STAY">숙소</option>
            <option value="RESTAURANT">식당</option>
            <option value="CAFE">카페</option>
            <option value="ETC">기타</option>
          </select>
        </div>

        {/* 단일 이미지 업로드 UI */}
        <div className="mb-4">
          <label className="block text-gray-700 mb-1">이미지</label>
          <div className="mt-2">
            <label className="flex justify-center items-center border-2 border-dashed border-gray-300 rounded-md p-6 cursor-pointer hover:bg-gray-50">
              <div className="space-y-1 text-center">
                <Upload className="mx-auto h-12 w-12 text-gray-400" />
                <div className="text-sm text-gray-600">
                  <span className="font-medium text-blue-600 hover:text-blue-500">
                    파일 선택
                  </span>{" "}
                  또는 드래그 앤 드롭
                </div>
                <p className="text-xs text-gray-500">PNG, JPG, GIF 최대 10MB</p>
              </div>
              <input
                type="file"
                className="hidden"
                accept="image/*"
                onChange={handleImageUpload}
              />
            </label>
          </div>
          {previewImage && (
            <div className="mt-4 relative">
              <img
                src={previewImage}
                alt="Preview"
                className="h-24 w-24 object-cover rounded-md"
              />
              <button
                type="button"
                onClick={handleRemoveImage}
                className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
          )}
        </div>

        <button
          type="submit"
          className="bg-blue-500 text-white px-4 py-2 rounded-md"
        >
          등록하기
        </button>
      </form>
    </div>
  );
}
