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
}