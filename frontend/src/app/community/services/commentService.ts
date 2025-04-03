import { api } from "../utils/api";

// 댓글 관련 타입 정의
export interface Comment {
  commentId: number;
  content: string;
  reviewId: number;
  memberId: number;
  memberName: string;
  profileImage?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CommentRequestDto {
  content: string;
  reviewId: number;
}

// 리뷰의 댓글 목록 조회
export async function getCommentsByReviewId(
  reviewId: number
): Promise<Comment[]> {
  try {
    const comments = await api.get<Comment[]>(
      `/api/comments/review/${reviewId}`
    );
    return comments || [];
  } catch (error) {
    console.error(`리뷰 ID ${reviewId}의 댓글 목록 조회 중 오류 발생:`, error);
    return [];
  }
}

// 댓글 생성
export async function createComment(
  comment: CommentRequestDto
): Promise<Comment> {
  try {
    const createdComment = await api.post<Comment>("/api/comments", comment);
    return createdComment;
  } catch (error) {
    console.error("댓글 생성 중 오류 발생:", error);
    throw error;
  }
}

// 댓글 수정
export async function updateComment(
  commentId: number,
  content: string
): Promise<Comment> {
  try {
    const updatedComment = await api.put<Comment>(
      `/api/comments/${commentId}`,
      { content }
    );
    return updatedComment;
  } catch (error) {
    console.error(`댓글 ID ${commentId} 수정 중 오류 발생:`, error);
    throw error;
  }
}

// 댓글 삭제
export async function deleteComment(commentId: number): Promise<void> {
  try {
    await api.delete(`/api/comments/${commentId}`);
  } catch (error) {
    console.error(`댓글 ID ${commentId} 삭제 중 오류 발생:`, error);
    throw error;
  }
}

// 내 댓글 조회
export async function getMyComments(): Promise<Comment[]> {
  try {
    const myComments = await api.get<Comment[]>("/api/comments/my");
    return myComments || [];
  } catch (error) {
    console.error("내 댓글 조회 중 오류 발생:", error);
    return [];
  }
}

// 댓글 상세 정보 조회
export async function getCommentById(commentId: number): Promise<Comment> {
  try {
    const comment = await api.get<Comment>(`/api/comments/${commentId}`);
    return comment;
  } catch (error) {
    console.error(`댓글 ID ${commentId} 조회 중 오류 발생:`, error);
    throw error;
  }
}
