"use client";

import type React from "react";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import {
  Star,
  Calendar,
  Eye,
  MessageSquare,
  MapPin,
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
import {
  getReviewById,
  deleteReview,
  ReviewDetail as ReviewDetailType,
} from "./services/reviewService";
import {
  getCommentsByReviewId,
  createComment,
  updateComment,
  deleteComment,
  Comment,
} from "./services/commentService";
import { getCurrentUserInfo, isLoggedIn } from "./services/authService";
import { getPlaceById } from "./services/placeService";
import {
  getProfileImageUrl,
  handleProfileImageError,
} from "./utils/profileImageUtil";

export default function ReviewDetail({ id }: { id: string }) {
  const router = useRouter();
  const [review, setReview] = useState<ReviewDetailType | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [commentText, setCommentText] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isUserLoggedIn, setIsUserLoggedIn] = useState(false);
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);
  const [editingCommentId, setEditingCommentId] = useState<number | null>(null);
  const [editCommentText, setEditCommentText] = useState("");
  const [placeName, setPlaceName] = useState<string | null>(null);

  // 로그인 상태 및 사용자 정보 확인
  useEffect(() => {
    const checkLoginStatus = async () => {
      const loggedIn = isLoggedIn();
      setIsUserLoggedIn(loggedIn);

      if (loggedIn) {
        try {
          const userInfo = await getCurrentUserInfo();
          if (userInfo) {
            setCurrentUserId(userInfo.id);
          }
        } catch (err) {
          console.error("사용자 정보를 가져오는 중 오류가 발생했습니다:", err);
        }
      }
    };

    checkLoginStatus();
  }, []);

  // 리뷰와 댓글 가져오기
  useEffect(() => {
    const fetchReviewAndComments = async () => {
      setLoading(true);
      try {
        // 리뷰 데이터 가져오기
        const reviewId = parseInt(id);
        const reviewData = await getReviewById(reviewId);
        setReview(reviewData);

        // 여행지 정보 가져오기 관련 부분 수정
        if (reviewData.placeId) {
          try {
            // 1. 리뷰 객체에 이미 placeName이 있다면 그대로 사용
            if (reviewData.placeName) {
              setPlaceName(reviewData.placeName);
            }
            // 2. 없는 경우에만 API로 가져오기 시도
            else {
              const placeData = await getPlaceById(reviewData.placeId);
              if (placeData) {
                setPlaceName(placeData.placeName);
              } else {
                // 3. API 호출 실패시 기본값 설정
                setPlaceName(`여행지 ${reviewData.placeId}`);
              }
            }
          } catch (placeError) {
            console.error("여행지 정보를 불러오는 중 오류 발생:", placeError);
            // 오류 발생시 기본값 설정
            setPlaceName(
              reviewData.placeName || `여행지 ${reviewData.placeId}`
            );
          }
        }

        // 댓글 데이터 가져오기
        const commentsData = await getCommentsByReviewId(reviewId);
        setComments(commentsData);
      } catch (err) {
        console.error("리뷰 정보를 불러오는 중 오류가 발생했습니다:", err);
        setError("리뷰 정보를 불러오는 중 오류가 발생했습니다.");
      } finally {
        setLoading(false);
      }
    };

    fetchReviewAndComments();
  }, [id]);

  // 댓글 제출 처리
  const handleCommentSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!commentText.trim()) return;
    if (!isUserLoggedIn) {
      alert("댓글을 작성하려면 로그인이 필요합니다.");
      router.push("/member/login");
      return;
    }

    setIsSubmitting(true);

    try {
      const commentData = {
        content: commentText,
        reviewId: parseInt(id),
      };

      const newComment = await createComment(commentData);

      // 댓글 목록 갱신
      setComments([...comments, newComment]);
      setCommentText("");

      // 리뷰의 댓글 수 갱신
      if (review) {
        setReview({
          ...review,
          commentCount: review.commentCount + 1,
        });
      }
    } catch (err) {
      console.error("댓글 작성 중 오류가 발생했습니다:", err);
      alert("댓글 작성 중 오류가 발생했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  // 댓글 수정 처리
  const handleEditComment = (commentId: number, content: string) => {
    setEditingCommentId(commentId);
    setEditCommentText(content);
  };

  // 댓글 수정 완료 처리
  const handleUpdateComment = async (commentId: number) => {
    if (!editCommentText.trim()) return;

    try {
      const updatedComment = await updateComment(commentId, editCommentText);

      // 댓글 목록 갱신
      setComments(
        comments.map((comment) =>
          comment.commentId === commentId
            ? { ...comment, content: editCommentText }
            : comment
        )
      );

      setEditingCommentId(null);
      setEditCommentText("");
    } catch (err) {
      console.error("댓글 수정 중 오류가 발생했습니다:", err);
      alert("댓글 수정 중 오류가 발생했습니다.");
    }
  };

  // 댓글 삭제 처리
  const handleDeleteComment = async (commentId: number) => {
    try {
      await deleteComment(commentId);

      // 댓글 목록 갱신
      setComments(
        comments.filter((comment) => comment.commentId !== commentId)
      );

      // 리뷰의 댓글 수 갱신
      if (review) {
        setReview({
          ...review,
          commentCount: review.commentCount - 1,
        });
      }
    } catch (err) {
      console.error("댓글 삭제 중 오류가 발생했습니다:", err);
      alert("댓글 삭제 중 오류가 발생했습니다.");
    }
  };

  // 리뷰 삭제 처리
  const handleDeleteReview = async () => {
    try {
      await deleteReview(parseInt(id));
      alert("리뷰가 삭제되었습니다.");
      router.push("/community");
    } catch (err) {
      console.error("리뷰 삭제 중 오류가 발생했습니다:", err);
      alert("리뷰 삭제 중 오류가 발생했습니다.");
    }
  };

  // 날짜 형식 지정
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(
      2,
      "0"
    )}.${String(date.getDate()).padStart(2, "0")} ${String(
      date.getHours()
    ).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
  };

  // 별점 렌더링
  const renderStars = (rating: number) => {
    return Array.from({ length: 5 }).map((_, i) => (
      <Star
        key={i}
        className={`h-5 w-5 ${
          i < rating ? "text-yellow-400 fill-yellow-400" : "text-gray-300"
        }`}
      />
    ));
  };

  if (loading) {
    return <div className="text-center py-10">리뷰를 불러오는 중...</div>;
  }

  if (error || !review) {
    return (
      <div className="text-center py-10 text-red-500">
        {error || "리뷰를 찾을 수 없습니다."}
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      {/* 뒤로가기 버튼 */}
      <div className="mb-6">
        <Button variant="ghost" onClick={() => router.push("/community")}>
          <ArrowLeft className="h-4 w-4 mr-2" />
          리뷰 목록으로 돌아가기
        </Button>
      </div>

      {/* 리뷰 카드 */}
      <Card className="mb-8">
        <CardHeader className="pb-3">
          <div className="flex justify-between items-start">
            <div>
              <div className="flex items-center gap-2 mb-2">
                <MapPin className="h-4 w-4 text-gray-500" />
                <Link
                  href={`/place/${review.placeId}`}
                  className="text-sm text-blue-600 hover:underline"
                >
                  {placeName || review.placeName || `여행지 ${review.placeId}`}
                </Link>
              </div>
              <h1 className="text-2xl font-bold mb-2">{review.title}</h1>
              <div className="flex items-center mb-1">
                {renderStars(review.rating)}
              </div>
            </div>

            {/* 수정/삭제 버튼 (작성자만 볼 수 있음) */}
            {currentUserId === review.memberId && (
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() =>
                    router.push(`/community/edit/${review.reviewId}`)
                  }
                >
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
                      <AlertDialogTitle>리뷰 삭제</AlertDialogTitle>
                      <AlertDialogDescription>
                        이 리뷰를 정말 삭제하시겠습니까? 이 작업은 되돌릴 수
                        없습니다.
                      </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                      <AlertDialogCancel>취소</AlertDialogCancel>
                      <AlertDialogAction onClick={handleDeleteReview}>
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
                  src={getProfileImageUrl(review.profileImage)}
                  alt={review.memberName}
                  onError={handleProfileImageError}
                />
                <AvatarFallback>
                  {review.memberName.substring(0, 2)}
                </AvatarFallback>
              </Avatar>
              <span className="font-medium">{review.memberName}</span>
            </div>
            <div className="flex items-center gap-4">
              <div>
                <Calendar className="h-4 w-4 inline mr-1" />
                <span>{formatDate(review.createdAt)}</span>
              </div>
              <div>
                <Eye className="h-4 w-4 inline mr-1" />
                <span>{review.viewCount}</span>
              </div>
              <div>
                <MessageSquare className="h-4 w-4 inline mr-1" />
                <span>{review.commentCount}</span>
              </div>
            </div>
          </div>
        </CardHeader>

        <CardContent>
          {/* 리뷰 내용 */}
          <div className="prose max-w-none mb-6">
            {review.content.split("\n\n").map((paragraph, idx) => (
              <p key={idx} className="mb-4">
                {paragraph}
              </p>
            ))}
          </div>

          {/* 리뷰 이미지 (백엔드 API가 구현된 경우 활성화) */}
          {/* {review.images && review.images.length > 0 && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
              {review.images.map((image, idx) => (
                <img
                  key={idx}
                  src={image || "/placeholder.svg"}
                  alt={`${review.title} 이미지 ${idx + 1}`}
                  className="rounded-md w-full h-auto object-cover"
                />
              ))}
            </div>
          )} */}
        </CardContent>
      </Card>

      {/* 댓글 섹션 */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold mb-6">댓글 {comments.length}개</h2>

        {/* 댓글 목록 */}
        {comments.length > 0 ? (
          <div className="space-y-6 mb-6">
            {comments.map((comment) => (
              <div
                key={comment.commentId}
                className="border-b pb-4 last:border-0 last:pb-0"
              >
                <div className="flex justify-between items-start mb-2">
                  <div className="flex items-center">
                    <Avatar className="h-8 w-8 mr-2">
                      <AvatarImage
                        src={getProfileImageUrl(comment.profileImage)}
                        alt={comment.memberName}
                        onError={handleProfileImageError}
                      />
                      <AvatarFallback>
                        {comment.memberName.substring(0, 2)}
                      </AvatarFallback>
                    </Avatar>
                    <div>
                      <div className="font-medium">{comment.memberName}</div>
                      <div className="text-xs text-gray-500">
                        {formatDate(comment.createdAt)}
                      </div>
                    </div>
                  </div>

                  {/* 댓글 작업 (작성자만 볼 수 있음) */}
                  {currentUserId === comment.memberId && (
                    <div className="flex gap-2">
                      {editingCommentId === comment.commentId ? (
                        <>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() =>
                              handleUpdateComment(comment.commentId)
                            }
                          >
                            저장
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => setEditingCommentId(null)}
                          >
                            취소
                          </Button>
                        </>
                      ) : (
                        <>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() =>
                              handleEditComment(
                                comment.commentId,
                                comment.content
                              )
                            }
                          >
                            수정
                          </Button>
                          <AlertDialog>
                            <AlertDialogTrigger asChild>
                              <Button variant="ghost" size="sm">
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
                                  onClick={() =>
                                    handleDeleteComment(comment.commentId)
                                  }
                                >
                                  삭제
                                </AlertDialogAction>
                              </AlertDialogFooter>
                            </AlertDialogContent>
                          </AlertDialog>
                        </>
                      )}
                    </div>
                  )}
                </div>

                {editingCommentId === comment.commentId ? (
                  <Textarea
                    value={editCommentText}
                    onChange={(e) => setEditCommentText(e.target.value)}
                    className="w-full mt-2"
                  />
                ) : (
                  <p className="text-gray-700 mt-2">{comment.content}</p>
                )}
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-8 text-gray-500">
            아직 댓글이 없습니다. 첫 댓글을 작성해보세요!
          </div>
        )}

        <Separator className="my-6" />

        {/* 댓글 폼 */}
        <form onSubmit={handleCommentSubmit}>
          <div className="mb-4">
            <Textarea
              placeholder={
                isUserLoggedIn
                  ? "댓글을 작성해주세요..."
                  : "댓글을 작성하려면 로그인이 필요합니다."
              }
              value={commentText}
              onChange={(e) => setCommentText(e.target.value)}
              disabled={!isUserLoggedIn || isSubmitting}
              className="w-full"
            />
          </div>
          <div className="flex justify-end">
            <Button
              type="submit"
              disabled={!isUserLoggedIn || !commentText.trim() || isSubmitting}
            >
              {isSubmitting ? "등록 중..." : "댓글 등록"}
              <Send className="h-4 w-4 ml-2" />
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
