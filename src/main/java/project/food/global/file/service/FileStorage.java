package project.food.global.file.service;

import org.springframework.web.multipart.MultipartFile;
import project.food.global.file.dto.UploadedFileInfo;

import java.util.List;

public interface FileStorage {
    UploadedFileInfo savePostImage(MultipartFile file, Long memberId);
    UploadedFileInfo saveProfileImage(MultipartFile file, Long memberId);
    List<UploadedFileInfo> storeFiles(List<MultipartFile> files, Long memberId);
    void deleteFile(String path);
    void deleteFiles(List<String> paths);
    // 네이버에서 다운로드한 이미지(byte[])를 저장 후 URL 반환
    String saveRestaurantImage(byte[] imageBytes, String fileName);
}