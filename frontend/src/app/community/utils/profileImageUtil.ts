export const getProfileImageUrl = (imagePath?: string | null): string => {
    // 기본 이미지 경로
    const defaultImagePath = "/defaultUser.png";

    // 이미지 경로가 없거나, null이거나, 빈 문자열인 경우 기본 이미지 반환
    if (!imagePath) {
        return defaultImagePath;
    }

    // 백엔드 서버 URL (환경 변수에서 가져오거나 기본값 사용)
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

    // 이미 완전한 URL인 경우 그대로 사용
    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
        return imagePath;
    }

    // 이미지 경로가 /images/로 시작하면 API URL과 결합
    if (imagePath.startsWith("/images/")) {
        return `${apiUrl}${imagePath}`;
    }

    // 그 외의 경우 - 기본 경로 가정
    return imagePath || defaultImagePath;
};

//프로필 이미지 렌더링 시 에러 처리 함수
export const handleProfileImageError = (e: React.SyntheticEvent<HTMLImageElement, Event>) => {
    e.currentTarget.src = "/defaultUser.png";
};