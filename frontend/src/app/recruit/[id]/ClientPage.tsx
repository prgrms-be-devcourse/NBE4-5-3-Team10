"use client";

import { useEffect, useState } from "react";
import { useRouter, useParams } from "next/navigation";
import { fetchWithAuth } from "@/lib/auth";
import Link from "next/link";
import {
  Calendar,
  MapPin,
  Users,
  DollarSign,
  Briefcase,
  ArrowLeft,
  Edit,
  Trash2,
  Send,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Separator } from "@/components/ui/separator";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { Badge } from "@/components/ui/badge";

const API_BASE_URL = `${process.env.NEXT_PUBLIC_API_URL}/recruits`;
const USER_INFO_URL = `${process.env.NEXT_PUBLIC_API_URL}/member/mypage`;

// 댓글 타입 정의
interface CommentType {
  applyId: number;
  memberId: number;
  memberProfileImage: string;
  memberNickname: string;
  content: string;
  createdAt: string;
  updatedAt: string;
}

// 모집 상세 타입 정의
interface RecruitDetail {
  recruitId: number;
  memberProfileImage: string;
  memberNickname: string;
  genderRestriction: string;
  ageRestriction: string;
  placeCityName: string;
  placePlaceName: string;
  title: string;
  content: string;
  isClosed: boolean;
  startDate: string;
  endDate: string;
  travelStyle: string;
  budget: number;
  groupSize: number;
  createdAt: string;
  updatedAt: string;
  applies: CommentType[];
  memberId: number;
  placeId: number;
}

export default function RecruitDetailPage() {
  const [recruit, setRecruit] = useState<RecruitDetail>({
    recruitId: 0,
    memberProfileImage: "",
    memberNickname: "",
    genderRestriction: "",
    ageRestriction: "",
    placeCityName: "",
    placePlaceName: "",
    title: "",
    content: "",
    isClosed: false,
    startDate: "",
    endDate: "",
    travelStyle: "",
    budget: 0,
    groupSize: 0,
    createdAt: "",
    updatedAt: "",
    applies: [],
    memberId: 0,
    placeId: 0,
  });

  const [myMemberId, setMyMemberId] = useState<number | null>(null);
  const [myMemberAuthority, setMyMemberAuthority] = useState<string | null>(
    null
  );
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();
  const params = useParams();
  const recruitId = params.id;
  const [commentContent, setCommentContent] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!recruitId) return;

    async function fetchRecruit() {
      setLoading(true);
      try {
        const response = await fetchWithAuth(`${API_BASE_URL}/${recruitId}`);
        if (!response.ok) throw new Error("모집글을 불러오는 데 실패했습니다.");
        const data = await response.json();
        setRecruit(data.data);
        setError(null);
      } catch (error) {
        console.error("❌ 모집글 조회 오류:", error);
        setError("모집글을 불러오는 중 오류가 발생했습니다.");
      } finally {
        setLoading(false);
      }
    }

    const fetchMyInfo = async () => {
      const token = localStorage.getItem("accessToken");

      if (!token) {
        console.warn("🚫 로그인하지 않은 사용자입니다.");
        return;
      }

      try {
        const response = await fetchWithAuth(USER_INFO_URL);
        const data = await response.json();

        if (!response.ok || !data.data.id) {
          throw new Error("유저 정보를 가져오지 못했습니다.");
        }

        setMyMemberId(data.data.id);
        setMyMemberAuthority(data.data.authority);
        console.log("authority");
        console.log(data.data.authority);
      } catch (error) {
        console.error("❌ 유저 정보 조회 오류:", error);
      }
    };

    fetchRecruit();
    fetchMyInfo();
  }, [recruitId]);

  const handleEdit = () => {
    router.push(`/recruit/edit/${recruit.recruitId}`);
  };

  const handleDelete = async () => {
    try {
      const response = await fetchWithAuth(
        `${API_BASE_URL}/${recruit.recruitId}`,
        {
          method: "DELETE",
        }
      );
      if (!response.ok) throw new Error("삭제 실패");
      alert("삭제 완료!");
      router.push("/recruit/list");
    } catch (error) {
      console.error("❌ 모집글 삭제 오류:", error);
    }
  };

  const handleCommentSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!commentContent.trim()) {
      alert("댓글을 입력해주세요!");
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await fetchWithAuth(
        `${API_BASE_URL}/${recruitId}/applies`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ content: commentContent }),
        }
      );

      if (!response.ok) throw new Error("댓글 등록 실패");

      const newComment = await response.json();

      setRecruit((prev) => ({
        ...prev,
        applies: [...prev.applies, newComment.data],
      }));

      setCommentContent("");
    } catch (error) {
      console.error("❌ 댓글 등록 오류:", error);
      alert("댓글 등록 중 오류가 발생했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteComment = async (commentId: number) => {
    if (!window.confirm("댓글을 삭제하시겠습니까?")) return;

    try {
      const response = await fetchWithAuth(
        `${API_BASE_URL}/${recruitId}/applies/${commentId}`,
        {
          method: "DELETE",
        }
      );

      if (!response.ok) throw new Error("댓글 삭제 실패");

      setRecruit((prev) => ({
        ...prev,
        applies: prev.applies.filter(
          (comment) => comment.applyId !== commentId
        ),
      }));
    } catch (error) {
      console.error("❌ 댓글 삭제 오류:", error);
    }
  };

  // 날짜 포맷 함수 (YYYY.MM.DD HH:mm)
  const formatDateTime = (dateString: string) => {
    const date = new Date(dateString);
    return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(
      2,
      "0"
    )}.${String(date.getDate()).padStart(2, "0")} ${String(
      date.getHours()
    ).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
  };

  if (loading) {
    return <div className="text-center py-10">모집글을 불러오는 중...</div>;
  }

  if (error || !recruit) {
    return (
      <div className="text-center py-10 text-red-500">
        {error || "모집글을 찾을 수 없습니다."}
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      {/* Back button */}
      <div className="mb-6">
        <Button variant="ghost" onClick={() => router.push("/recruit/list")}>
          <ArrowLeft className="h-4 w-4 mr-2" />
          모집글 목록으로 돌아가기
        </Button>
      </div>

      {/* Recruit Card */}
      <Card className="mb-8">
        <CardHeader className="pb-3">
          <div className="flex justify-between items-start">
            <div>
              <div className="flex items-center gap-2 mb-2">
                <MapPin className="h-4 w-4 text-gray-500" />
                <Link
                  href={`/place/${recruit.placeId}`}
                  className="text-sm text-blue-600 hover:underline"
                >
                  {recruit.placeCityName}, {recruit.placePlaceName}
                </Link>
              </div>
              <h1 className="text-2xl font-bold mb-2">{recruit.title}</h1>

              {/* Status badges */}
              <div className="flex flex-wrap gap-2 mb-2">
                <Badge
                  variant={recruit.isClosed ? "destructive" : "default"}
                  className="rounded-full"
                >
                  {recruit.isClosed ? "모집 마감" : "모집 중"}
                </Badge>
                {recruit.genderRestriction !== "모든 성별" && (
                  <Badge
                    variant="outline"
                    className="rounded-full bg-blue-100 text-blue-600 border-blue-200"
                  >
                    {recruit.genderRestriction}
                  </Badge>
                )}
                {recruit.ageRestriction !== "모든 연령대" && (
                  <Badge
                    variant="outline"
                    className="rounded-full bg-green-100 text-green-600 border-green-200"
                  >
                    {recruit.ageRestriction}
                  </Badge>
                )}
                <Badge
                  variant="outline"
                  className="rounded-full bg-purple-100 text-purple-600 border-purple-200"
                >
                  {recruit.travelStyle}
                </Badge>
              </div>
            </div>

            {/* Edit/Delete buttons (only visible to author) */}
            {myMemberId !== null &&
              (recruit.memberId === myMemberId ||
                myMemberAuthority == "ADMIN") && (
                <div className="flex gap-2">
                  <Button variant="outline" size="sm" onClick={handleEdit}>
                    <Edit className="h-4 w-4 mr-1" />
                    수정
                  </Button>

                  <AlertDialog>
                    <AlertDialogTrigger asChild>
                      <Button variant="destructive" size="sm">
                        <Trash2 className="h-4 w-4 mr-1" />
                        삭제
                      </Button>
                    </AlertDialogTrigger>
                    <AlertDialogContent>
                      <AlertDialogHeader>
                        <AlertDialogTitle>모집글 삭제</AlertDialogTitle>
                        <AlertDialogDescription>
                          이 모집글을 정말 삭제하시겠습니까? 이 작업은 되돌릴 수
                          없습니다.
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <AlertDialogFooter>
                        <AlertDialogCancel>취소</AlertDialogCancel>
                        <AlertDialogAction onClick={handleDelete}>
                          삭제
                        </AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
                </div>
              )}
          </div>

          <div className="flex justify-between items-center text-sm text-gray-500 mt-2">
            <div className="flex items-center">
              <Avatar className="h-6 w-6 mr-2">
                <AvatarImage
                  src={recruit.memberProfileImage || "/default-profile.png"}
                  alt={recruit.memberNickname}
                />
                <AvatarFallback>
                  {recruit.memberNickname.substring(0, 2)}
                </AvatarFallback>
              </Avatar>
              <span className="font-medium">{recruit.memberNickname}</span>
            </div>
            <div className="flex items-center gap-4">
              <div>
                <Calendar className="h-4 w-4 inline mr-1" />
                <span>{formatDateTime(recruit.createdAt)}</span>
              </div>
              {recruit.createdAt !== recruit.updatedAt && (
                <div className="text-gray-400 text-xs">
                  (수정됨: {formatDateTime(recruit.updatedAt)})
                </div>
              )}
            </div>
          </div>
        </CardHeader>

        <CardContent>
          {/* Recruit details */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6 p-4 bg-gray-50 rounded-lg">
            <div className="flex items-center gap-2">
              <Calendar className="h-5 w-5 text-blue-500" />
              <div>
                <div className="text-xs text-gray-500">여행 일정</div>
                <div>
                  {recruit.startDate} ~ {recruit.endDate}
                </div>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Users className="h-5 w-5 text-green-500" />
              <div>
                <div className="text-xs text-gray-500">모집 인원</div>
                <div>{recruit.groupSize}명</div>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <DollarSign className="h-5 w-5 text-amber-500" />
              <div>
                <div className="text-xs text-gray-500">예산</div>
                <div>
                  {recruit.budget ? recruit.budget.toLocaleString() : "미정"}원
                </div>
              </div>
            </div>
          </div>

          {/* Recruit content */}
          <div className="prose max-w-none mb-6">
            {recruit.content.split("\n").map((paragraph, idx) => (
              <p key={idx} className="mb-4">
                {paragraph}
              </p>
            ))}
          </div>

          {/* Join button
          <Button className="w-full mb-6" disabled={recruit.isClosed}>
            {recruit.isClosed ? "모집이 마감되었습니다" : "모집 참여하기"}
          </Button> */}
        </CardContent>
      </Card>

      {/* Comments Section */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold mb-6">
          댓글 {recruit.applies.length}개
        </h2>

        {/* Comment List */}
        {recruit.applies && recruit.applies.length > 0 ? (
          <div className="space-y-6 mb-6">
            {recruit.applies.map((comment) => (
              <div
                key={comment.applyId}
                className="border-b pb-4 last:border-0 last:pb-0"
              >
                <div className="flex justify-between items-start mb-2">
                  <div className="flex items-center">
                    <Avatar className="h-8 w-8 mr-2">
                      <AvatarImage
                        src={
                          comment.memberProfileImage || "/default-profile.png"
                        }
                        alt={comment.memberNickname}
                      />
                      <AvatarFallback>
                        {comment.memberNickname.substring(0, 2)}
                      </AvatarFallback>
                    </Avatar>
                    <div>
                      <div className="font-medium">
                        {comment.memberNickname}
                      </div>
                      <div className="text-xs text-gray-500">
                        {formatDateTime(comment.createdAt)}
                      </div>
                    </div>
                  </div>

                  {/* Comment delete button (only for author) */}
                  {(comment.memberId === myMemberId ||
                    myMemberAuthority == "ADMIN") && (
                    <AlertDialog>
                      <AlertDialogTrigger asChild>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="text-red-500"
                        >
                          삭제
                        </Button>
                      </AlertDialogTrigger>
                      <AlertDialogContent>
                        <AlertDialogHeader>
                          <AlertDialogTitle>댓글 삭제</AlertDialogTitle>
                          <AlertDialogDescription>
                            이 댓글을 정말 삭제하시겠습니까?
                          </AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter>
                          <AlertDialogCancel>취소</AlertDialogCancel>
                          <AlertDialogAction
                            onClick={() => handleDeleteComment(comment.applyId)}
                          >
                            삭제
                          </AlertDialogAction>
                        </AlertDialogFooter>
                      </AlertDialogContent>
                    </AlertDialog>
                  )}
                </div>

                <p className="text-gray-700 mt-2">{comment.content}</p>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-8 text-gray-500">
            아직 댓글이 없습니다.
          </div>
        )}

        <Separator className="my-6" />

        {/* 🔹 로그인한 사용자만 댓글 입력 가능 */}
        {myMemberId !== null && (
          <form onSubmit={handleCommentSubmit}>
            <div className="mb-4">
              <Textarea
                placeholder="댓글을 작성해주세요..."
                value={commentContent}
                onChange={(e) => setCommentContent(e.target.value)}
                disabled={isSubmitting}
                className="w-full"
              />
            </div>
            <div className="flex justify-end">
              <Button
                type="submit"
                disabled={!commentContent.trim() || isSubmitting}
              >
                {isSubmitting ? "등록 중..." : "댓글 등록"}
                <Send className="h-4 w-4 ml-2" />
              </Button>
            </div>
          </form>
        )}

        {/* Comment Form
        <form onSubmit={handleCommentSubmit}>
          <div className="mb-4">
            <Textarea
              placeholder="댓글을 작성해주세요..."
              value={commentContent}
              onChange={(e) => setCommentContent(e.target.value)}
              disabled={isSubmitting}
              className="w-full"
            />
          </div>
          <div className="flex justify-end">
            <Button
              type="submit"
              disabled={!commentContent.trim() || isSubmitting}
            >
              {isSubmitting ? "등록 중..." : "댓글 등록"}
              <Send className="h-4 w-4 ml-2" />
            </Button>
          </div>
        </form> */}
      </div>
    </div>
  );
}

// "use client";

// import { useEffect, useState } from "react";
// import { useRouter, useParams } from "next/navigation";
// import { getRecruitById } from "@/lib/api/recruit";
// import { fetchWithAuth } from "@/lib/auth";
// import Header from "@/components/Header";
// import Footer from "@/components/Footer";

// const API_BASE_URL = `${process.env.NEXT_PUBLIC_API_URL}/recruits`;
// const USER_INFO_URL = `${process.env.NEXT_PUBLIC_API_URL}/member/mypage`;

// // 댓글 타입 정의
// interface CommentType {
//   applyId: number;
//   memberId: number;
//   memberProfileImage: string;
//   memberNickname: string;
//   content: string;
//   createdAt: string;
//   updatedAt: string;
// }

// // 모집 상세 타입 정의
// interface RecruitDetail {
//   recruitId: number;
//   memberProfileImage: string;
//   memberNickname: string;
//   genderRestriction: string;
//   ageRestriction: string;
//   placeCityName: string;
//   placePlaceName: string;
//   title: string;
//   content: string;
//   isClosed: boolean;
//   startDate: string;
//   endDate: string;
//   travelStyle: string;
//   budget: number;
//   groupSize: number;
//   createdAt: string;
//   updatedAt: string;
//   applies: CommentType[]; // ✅ 댓글 목록 추가
//   memberId: number;
//   placeId: number;
// }

// export default function RecruitDetailPage(/*{
//   params,
// }: {
//   params: { id: string };
// }*/) {
//   //   const [recruit, setRecruit] = useState<RecruitDetail | null>(null);
//   const [recruit, setRecruit] = useState<RecruitDetail>({
//     recruitId: 0,
//     memberProfileImage: "",
//     memberNickname: "",
//     genderRestriction: "",
//     ageRestriction: "",
//     placeCityName: "",
//     placePlaceName: "",
//     title: "",
//     content: "",
//     isClosed: false,
//     startDate: "",
//     endDate: "",
//     travelStyle: "",
//     budget: 0,
//     groupSize: 0,
//     createdAt: "",
//     updatedAt: "",
//     applies: [], // ✅ 기본값을 빈 배열로 설정
//     memberId: 0,
//     placeId: 0,
//   });

//   const [myMemberId, setMyMemberId] = useState<number | null>(null);
//   const router = useRouter();
//   const params = useParams(); // ✅ Next.js 최신 버전에서는 useParams() 사용
//   const recruitId = params.id; // 🔹 비동기적으로 가져오기
//   const [commentContent, setCommentContent] = useState(""); // ✅ 댓글 입력 상태

//   useEffect(() => {
//     if (!recruitId) return; // ✅ params.id가 없을 경우 실행하지 않음

//     async function fetchRecruit() {
//       console.log(recruitId);
//       try {
//         const response = await fetchWithAuth(`${API_BASE_URL}/${recruitId}`);
//         if (!response.ok) throw new Error("모집글을 불러오는 데 실패했습니다.");
//         const data = await response.json();
//         console.log("Fetched recruit data:", data.data); // ✅ 디버깅용 로그
//         setRecruit(data.data);
//         console.log(recruit);
//       } catch (error) {
//         console.error("❌ 모집글 조회 오류:", error);
//       }
//     }

//     const fetchMyInfo = async () => {
//       const token = localStorage.getItem("accessToken"); // 🔹 로컬스토리지에서 토큰 확인

//       // ✅ 로그인하지 않은 경우 요청 안 함
//       if (!token) {
//         console.warn("🚫 로그인하지 않은 사용자입니다.");
//         return;
//       }

//       try {
//         const response = await fetchWithAuth(USER_INFO_URL);
//         const data = await response.json();
//         console.log("📢 서버에서 받아온 유저 정보:", data.data); // ✅ 응답 데이터 확인
//         console.log("📢 data.id:", data.data.id); // ✅ id 값이 존재하는지 확인

//         if (!response.ok || !data.data.id) {
//           throw new Error("유저 정보를 가져오지 못했습니다.");
//         }

//         setMyMemberId(data.data.id);
//       } catch (error) {
//         console.error("❌ 유저 정보 조회 오류:", error);
//       }
//     };

//     fetchRecruit();
//     fetchMyInfo();
//   }, [params.id]);

//   useEffect(() => {
//     console.log("Updated recruit state:", recruit);
//     console.log("Updated myMemberId state:", myMemberId);
//   }, [recruit, myMemberId]); // ✅ recruit와 myMemberId가 업데이트될 때 로그 확인

//   if (!recruit) return <p>로딩 중...</p>;

//   const handleEdit = () => {
//     router.push(`/recruit/edit/${recruit.recruitId}`);
//   };

//   const handleDelete = async () => {
//     if (!window.confirm("정말 삭제하시겠습니까?")) return;

//     try {
//       const response = await fetchWithAuth(
//         `${API_BASE_URL}/${recruit.recruitId}`,
//         {
//           method: "DELETE",
//         }
//       );
//       if (!response.ok) throw new Error("삭제 실패");
//       alert("삭제 완료!");
//       router.push("/recruit/list");
//     } catch (error) {
//       console.error("❌ 모집글 삭제 오류:", error);
//     }
//   };

//   const handleCommentSubmit = async () => {
//     if (!commentContent.trim()) {
//       alert("댓글을 입력해주세요!");
//       return;
//     }

//     try {
//       const response = await fetchWithAuth(
//         `${API_BASE_URL}/${recruitId}/applies`,
//         {
//           method: "POST",
//           headers: { "Content-Type": "application/json" },
//           body: JSON.stringify({ content: commentContent }),
//         }
//       );

//       if (!response.ok) throw new Error("댓글 등록 실패");

//       const newComment = await response.json(); // ✅ 백엔드에서 반환된 새 댓글 데이터를 받음

//       console.log("✅ 새 댓글 등록 완료:", newComment.data);

//       // ✅ recruit 상태를 새로운 댓글 데이터로 업데이트
//       setRecruit((prev) => ({
//         ...prev,
//         applies: [...prev.applies, newComment.data], // 백엔드에서 받은 `newComment`를 포함하여 업데이트
//       }));

//       setCommentContent(""); // 입력창 초기화
//     } catch (error) {
//       console.error("❌ 댓글 등록 오류:", error);
//     }
//   };

//   //   const handleCommentSubmit = async () => {
//   //     if (!commentContent.trim()) {
//   //       alert("댓글을 입력해주세요!");
//   //       return;
//   //     }

//   //     try {
//   //       const response = await fetchWithAuth(
//   //         `${API_BASE_URL}/${recruitId}/applies`,
//   //         {
//   //           method: "POST",
//   //           headers: { "Content-Type": "application/json" },
//   //           body: JSON.stringify({ content: commentContent }),
//   //         }
//   //       );

//   //       if (!response.ok) throw new Error("댓글 등록 실패");

//   //       const newComment = await response.json();

//   //       setRecruit((prev) => ({
//   //         ...prev,
//   //         applies: [...prev.applies, newComment], // ✅ 기존 댓글 목록에 추가
//   //       }));

//   //       setCommentContent(""); // 입력창 초기화
//   //     } catch (error) {
//   //       console.error("❌ 댓글 등록 오류:", error);
//   //     }
//   //   };

//   const handleDeleteComment = async (commentId: number) => {
//     if (!window.confirm("댓글을 삭제하시겠습니까?")) return;

//     try {
//       const response = await fetchWithAuth(
//         `${API_BASE_URL}/${recruitId}/applies/${commentId}`,
//         {
//           method: "DELETE",
//         }
//       );

//       if (!response.ok) throw new Error("댓글 삭제 실패");

//       setRecruit((prev) => ({
//         ...prev,
//         applies: prev.applies.filter(
//           (comment) => comment.applyId !== commentId
//         ), // ✅ 삭제된 댓글 제거
//       }));
//     } catch (error) {
//       console.error("❌ 댓글 삭제 오류:", error);
//     }
//   };

//   // 날짜 포맷 함수 (YYYY-MM-DD HH:mm)
//   const formatDateTime = (dateString: string) => {
//     const date = new Date(dateString);
//     return date.toLocaleString("ko-KR", {
//       year: "numeric",
//       month: "2-digit",
//       day: "2-digit",
//       hour: "2-digit",
//       minute: "2-digit",
//     });
//   };

//   return (
//     <div>
//       <div className="min-h-screen bg-gray-50 p-8 max-w-2xl mx-auto">
//         {/* 헤더 컴포넌트 사용 */}
//         <Header />
//         {/* 제목 */}
//         <h2 className="text-3xl font-bold mb-4">{recruit.title}</h2>

//         {/* 모집자 정보 */}
//         <div className="flex items-center space-x-4 mb-4">
//           <img
//             src={recruit.memberProfileImage || "/default-profile.png"}
//             alt="프로필 이미지"
//             className="w-12 h-12 rounded-full object-cover"
//           />
//           <div>
//             <p className="text-gray-700 font-semibold">
//               {recruit.memberNickname}
//             </p>
//             <p className="text-gray-500 text-sm">
//               작성일: {formatDateTime(recruit.createdAt)}
//             </p>
//             {recruit.createdAt !== recruit.updatedAt && (
//               <p className="text-gray-400 text-sm">
//                 수정됨: {formatDateTime(recruit.updatedAt)}
//               </p>
//             )}
//             {/* 내가 작성한 글일 때만 수정/삭제 버튼 표시 */}
//             {recruit &&
//               myMemberId !== null &&
//               recruit.memberId === myMemberId && (
//                 <div className="mt-6 flex space-x-4">
//                   <button
//                     onClick={handleEdit}
//                     className="px-4 py-2 bg-blue-500 text-white rounded"
//                   >
//                     수정
//                   </button>
//                   <button
//                     onClick={handleDelete}
//                     className="px-4 py-2 bg-red-500 text-white rounded"
//                   >
//                     삭제
//                   </button>
//                 </div>
//               )}
//           </div>
//         </div>

//         {/* 모집 정보 */}
//         <p className="text-gray-600">
//           🗺️ 여행지: {recruit.placeCityName}, {recruit.placePlaceName}
//         </p>
//         <p className="text-gray-600">
//           ⏳ 일정: {recruit.startDate} ~ {recruit.endDate}
//         </p>
//         <p className="text-gray-600">👥 모집 인원: {recruit.groupSize}명</p>
//         <p className="text-gray-600">
//           💰 예산: {recruit.budget ? recruit.budget.toLocaleString() : "미정"}원
//         </p>
//         <p className="text-gray-600">🎒 여행 스타일: {recruit.travelStyle}</p>

//         {/* 모집 상태 & 조건 */}
//         <div className="mt-4 flex space-x-2">
//           {/* 모집 상태 */}
//           <span
//             className={`px-2 py-1 text-xs rounded-full ${
//               recruit.isClosed
//                 ? "bg-red-100 text-red-600"
//                 : "bg-green-100 text-green-600"
//             }`}
//           >
//             {recruit.isClosed ? "모집 마감" : "모집 중"}
//           </span>

//           {/* 성별 제한 */}
//           {recruit.genderRestriction !== "모든 성별" && (
//             <span className="px-2 py-1 bg-blue-100 text-blue-600 text-xs rounded-full">
//               {recruit.genderRestriction}
//             </span>
//           )}

//           {/* 나이 제한 */}
//           {recruit.ageRestriction !== "모든 연령대" && (
//             <span className="px-2 py-1 bg-green-100 text-green-600 text-xs rounded-full">
//               {recruit.ageRestriction}
//             </span>
//           )}
//         </div>

//         {/* 내용 */}
//         <p className="mt-6 text-gray-700 whitespace-pre-line">
//           {recruit.content}
//         </p>

//         {/* 모집 참여 버튼 */}
//         <button
//           className="mt-6 w-full bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
//           onClick={() => alert("모집 참여 기능 구현 필요!")}
//         >
//           모집 참여하기
//         </button>

//         <div className="mt-6">
//           <textarea
//             value={commentContent}
//             onChange={(e) => setCommentContent(e.target.value)}
//             placeholder="댓글을 입력하세요..."
//             className="w-full p-2 border rounded"
//           ></textarea>
//           <button
//             onClick={handleCommentSubmit}
//             className="mt-2 px-4 py-2 bg-green-500 text-white rounded"
//           >
//             댓글 등록
//           </button>
//         </div>

//         {/* ✅ 댓글 목록 */}
//         <div className="mt-10">
//           <h3 className="text-2xl font-semibold mb-4">💬 댓글</h3>
//           {recruit.applies && recruit.applies.length === 0 ? (
//             <p className="text-gray-500">아직 댓글이 없습니다.</p>
//           ) : (
//             <ul className="space-y-4">
//               {recruit.applies.map((comment) => (
//                 <li
//                   key={comment.applyId || Math.random()} // ✅ key 속성 추가 (백엔드에서 `applyId`가 없을 경우 대비)
//                   className="p-4 bg-white shadow-md rounded-lg flex items-start space-x-4"
//                 >
//                   <img
//                     src={comment.memberProfileImage || "/default-profile.png"}
//                     alt="프로필 이미지"
//                     className="w-10 h-10 rounded-full object-cover"
//                   />
//                   <div>
//                     <p className="text-gray-700 font-semibold">
//                       {comment.memberNickname}
//                     </p>
//                     <p className="text-gray-600 mt-1">{comment.content}</p>
//                     <p className="text-gray-400 text-xs mt-1">
//                       작성일:{" "}
//                       {comment.createdAt
//                         ? formatDateTime(comment.createdAt)
//                         : "날짜 없음"}{" "}
//                       {/* ✅ 예외 처리 */}
//                     </p>
//                   </div>

//                   {/* 내가 작성한 댓글이면 삭제 버튼 표시 */}
//                   {comment.memberId === myMemberId && (
//                     <button
//                       onClick={() => handleDeleteComment(comment.applyId)}
//                       className="ml-auto px-2 py-1 text-red-500"
//                     >
//                       삭제
//                     </button>
//                   )}
//                 </li>
//               ))}
//             </ul>
//           )}
//         </div>
//         {/* 푸터 컴포넌트 사용 */}
//         <Footer />
//       </div>
//     </div>
//   );
// }
