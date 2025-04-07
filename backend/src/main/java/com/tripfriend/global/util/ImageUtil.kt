package com.tripfriend.global.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class ImageUtil {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String saveImage(MultipartFile imageFile) throws IOException {

        if (imageFile == null || imageFile.isEmpty()) {
            return null; // 이미지가 없으면 null 반환
        }

        // 디렉토리 생성 (없을 경우)
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 고유한 파일명 생성
        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);

        // 이미지 저장
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/images/" + fileName; // 저장된 파일의 상대 경로 반환
    }

    public void deleteImage(String imagePath) throws IOException {

        if (imagePath == null || imagePath.trim().isEmpty()) {
            return;
        }

        // "/images/" 로 시작하는 상대 경로에서 실제 파일명만 추출
        String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);

        // 실제 파일 경로 생성
        Path fullPath = Paths.get(uploadDir, fileName);
        File file = fullPath.toFile();

        if (file.exists() && !file.delete()) {
            throw new IOException("이미지 파일 삭제 실패: " + fullPath);
        }
    }
}
