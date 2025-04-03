import { api } from '../utils/api';
import { getProfileImageUrl } from '../utils/profileImageUtil';

/**
 * 프로필 이미지 업로드 함수
 * @param imageFile 업로드할 이미지 파일
 * @returns 업로드된 이미지 URL
 */
export async function uploadProfileImage(imageFile: File): Promise<string> {
    try {
        // FormData 객체 생성
        const formData = new FormData();
        formData.append('image', imageFile);

        // API 호출
        const imageUrl = await api.post<string>(
            '/member/profile-image/upload',
            formData,
            true // FormData 형식 사용
        );

        return imageUrl || '';
    } catch (error) {
        console.error('프로필 이미지 업로드 중 오류 발생:', error);
        throw error;
    }
}

/**
 * 프로필 이미지 삭제 함수
 */
export async function deleteProfileImage(): Promise<void> {
    try {
        await api.delete('/member/profile-image/delete');
    } catch (error) {
        console.error('프로필 이미지 삭제 중 오류 발생:', error);
        throw error;
    }
}

/**
 * 현재 사용자의 프로필 이미지 URL 가져오기
 */
export async function getCurrentUserProfileImage(): Promise<string> {
    try {
        const userInfo = await api.get<any>('/member/mypage');
        return getProfileImageUrl(userInfo?.profileImage);
    } catch (error) {
        console.error('프로필 이미지 정보 조회 중 오류 발생:', error);
        return getProfileImageUrl(null); // 기본 이미지 반환
    }
}