package project.food.global.file.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.food.global.config.FileStorageConfig;
import project.food.global.exception.ErrorCode;
import project.food.global.exception.FileUploadException;
import project.food.global.file.dto.UploadedFileInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Profile({"dev", "test"})
@RequiredArgsConstructor
@Slf4j
public class LocalFileStorage implements FileStorage {

    private final FileStorageConfig fileStorageConfig;

    private static final String BASE_DIR = "uploads";
    private static final String BASE_URL = "http://localhost:8080/uploads";
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");

    private UploadedFileInfo storeFile(MultipartFile file, String subDir, Long memberId) {
        validateFile(file);

        String originalFileName = file.getOriginalFilename();
        String storedFileName = generateStoredFileName(originalFileName);
        Path dirPath = Paths.get(BASE_DIR, subDir, String.valueOf(memberId));
        Path filePath = dirPath.resolve(storedFileName);

        try {
            Files.createDirectories(dirPath);
            file.transferTo(filePath.toAbsolutePath());

            String relativePath = subDir + "/" + memberId + "/" + storedFileName;
            String fileUrl = BASE_URL + "/" + relativePath;

            log.info("로컬 파일 저장: {}", filePath);

            return UploadedFileInfo.builder()
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .filePath(relativePath)
                    .fileSize(file.getSize())
                    .fileUrl(fileUrl)
                    .build();

        } catch (IOException e) {
            throw new FileUploadException(ErrorCode.FILE_UPLOAD_FAILED, "파일 저장 중 오류: " + originalFileName);
        }
    }

    @Override
    public UploadedFileInfo savePostImage(MultipartFile file, Long memberId) {
        return storeFile(file, "post", memberId);
    }

    @Override
    public UploadedFileInfo saveProfileImage(MultipartFile file, Long memberId) {
        return storeFile(file, "profile", memberId);
    }

    @Override
    public List<UploadedFileInfo> storeFiles(List<MultipartFile> files, Long memberId) {
        log.info("다중 파일 저장 시작: memberId={}, fileCount={}", memberId, files.size());

        if (files.size() > fileStorageConfig.getMaxFileCount()) {
            throw new FileUploadException(ErrorCode.FILE_COUNT_EXCEEDED);
        }

        List<UploadedFileInfo> uploadedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                uploadedFiles.add(savePostImage(file, memberId));
            }
        }

        log.info("다중 파일 저장 완료: memberId={}, savedCount={}", memberId, uploadedFiles.size());
        return uploadedFiles;
    }

    @Override
    public void deleteFile(String path) {
        try {
            Path filePath = Paths.get(BASE_DIR).resolve(path);
            Files.deleteIfExists(filePath);
            log.info("로컬 파일 삭제: {}", filePath);
        } catch (IOException e) {
            log.error("로컬 파일 삭제 실패: {}", path, e);
            throw new FileUploadException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    @Override
    public void deleteFiles(List<String> paths) {
        paths.forEach(path -> {
            try {
                deleteFile(path);
            } catch (Exception e) {
                log.error("로컬 파일 삭제 중 오류: {}", path);
            }
        });
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) throw new FileUploadException(ErrorCode.EMPTY_FILE);

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new FileUploadException(ErrorCode.INVALID_FILE_NAME);
        }

        String extension = getFileExtension(originalFileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileUploadException(ErrorCode.INVALID_FILE_TYPE);
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new FileUploadException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    private String generateStoredFileName(String originalFileName) {
        return UUID.randomUUID() + "." + getFileExtension(originalFileName);
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) throw new FileUploadException(ErrorCode.INVALID_FILE_NAME);
        return fileName.substring(lastDotIndex + 1);
    }
}