package project.food.global.file.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileStorageConfig fileStorageConfig;

    private static final List<String> ALLOWED_EXTENSIONS =
            List.of("jpg", "jpeg", "png", "gif", "webp");

    /**
     * 단일 파일 저장
     */
    public UploadedFileInfo storeFile(MultipartFile file) {
        validateFile(file);

        String originalFileName = file.getOriginalFilename();
        String storedFileName = generateStoredFileName(originalFileName);

        Path uploadPath = Paths.get(fileStorageConfig.getUploadDir())
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(uploadPath);
            Path targetPath = uploadPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetPath,
                    StandardCopyOption.REPLACE_EXISTING);

            log.info("✅ 파일 저장 성공: {}", storedFileName);

            return UploadedFileInfo.builder()
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .filePath(targetPath.toString())
                    .fileSize(file.getSize())
                    .fileUrl("/uploads/post/" + storedFileName)
                    .build();

        } catch (IOException e) {
            log.error("❌ 파일 저장 실패: {}", originalFileName, e);
            throw new FileUploadException(ErrorCode.FILE_UPLOAD_FAILED,
                    "파일 저장 중 오류: " + originalFileName);
        }
    }

    /**
     * 여러 파일 저장
     */
    public List<UploadedFileInfo> storeFiles(List<MultipartFile> files) {
        if (files.size() > fileStorageConfig.getMaxFileCount()) {
            throw new FileUploadException(ErrorCode.FILE_COUNT_EXCEEDED);
        }

        List<UploadedFileInfo> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                UploadedFileInfo fileInfo = storeFile(file);
                uploadedFiles.add(fileInfo);
            }
        }

        return uploadedFiles;
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String storedFileName) {
        try {
            Path filePath = Paths.get(fileStorageConfig.getUploadDir())
                    .resolve(storedFileName)
                    .normalize();

            Files.deleteIfExists(filePath);
            log.info("✅ 파일 삭제 성공: {}", storedFileName);

        } catch (IOException e) {
            log.error("❌ 파일 삭제 실패: {}", storedFileName, e);
            throw new FileUploadException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    /**
     * 여러 파일 삭제
     */
    public void deleteFiles(List<String> storedFileNames) {
        for (String fileName : storedFileNames) {
            deleteFile(fileName);
        }
    }

    /**
     * 파일 유효성 검증
     */
    private void validateFile(MultipartFile file) {
        // 1. 빈 파일 확인
        if (file.isEmpty()) {
            throw new FileUploadException(ErrorCode.EMPTY_FILE);
        }

        // 2. 파일명 확인
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new FileUploadException(ErrorCode.INVALID_FILE_NAME);
        }

        // 3. 확장자 검증
        String extension = getFileExtension(originalFileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileUploadException(ErrorCode.INVALID_FILE_TYPE);
        }

        // 4. 파일 크기 검증 (10MB)
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new FileUploadException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    /**
     * 저장될 파일명 생성
     */
    private String generateStoredFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + extension;
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new FileUploadException(ErrorCode.INVALID_FILE_NAME);
        }
        return fileName.substring(lastDotIndex + 1);
    }
}