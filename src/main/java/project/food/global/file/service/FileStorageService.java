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

        log.debug("파일 저장 시작: originalFileName={}, size ={}, bytes", file.getOriginalFilename(), file.getSize());

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

            log.info("✅ 파일 저장 성공: originalFileName={}, storedFileName={}, size={}",
                    originalFileName, storedFileName, file.getSize());

            return UploadedFileInfo.builder()
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .filePath(targetPath.toString())
                    .fileSize(file.getSize())
                    .fileUrl("/uploads/post/" + storedFileName)
                    .build();

        } catch (IOException e) {
            log.error("❌ 파일 저장 실패: originalFileName={}, error={}",
                    originalFileName, e.getMessage(), e);
            throw new FileUploadException(ErrorCode.FILE_UPLOAD_FAILED,
                    "파일 저장 중 오류: " + originalFileName);
        }
    }

    /**
     * 여러 파일 저장
     */
    public List<UploadedFileInfo> storeFiles(List<MultipartFile> files) {

        log.info("다중 파일 저장 시작: fileCount={}, maxAllowed={}",
                files.size(), fileStorageConfig.getMaxFileCount());

        if (files.size() > fileStorageConfig.getMaxFileCount()) {
            log.warn("⚠️ 파일 개수 초과: requestCount={}, maxAllowed={}",
                    files.size(), fileStorageConfig.getMaxFileCount());
            throw new FileUploadException(ErrorCode.FILE_COUNT_EXCEEDED);
        }

        List<UploadedFileInfo> uploadedFiles = new ArrayList<>();
        int successCount = 0;
        int emptyCount = 0;

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                UploadedFileInfo fileInfo = storeFile(file);
                uploadedFiles.add(fileInfo);
                successCount++;
            } else {
                emptyCount++;
                log.debug("빈 파일 건너뜀");
            }
        }

        log.info("✅ 다중 파일 저장 완료: totalFiles={}, successCount={}, emptyCount={}",
                files.size(), successCount, emptyCount);

        return uploadedFiles;
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String storedFileName) {

        log.debug("파일 삭제 시작: storedFileName={}", storedFileName);

        try {
            Path filePath = Paths.get(fileStorageConfig.getUploadDir())
                    .resolve(storedFileName)
                    .normalize();

            boolean deleted = Files.deleteIfExists(filePath);

            if (deleted) {
                log.info("✅ 파일 삭제 성공: storedFileName={}", storedFileName);
            } else {
                log.warn("⚠️ 파일이 존재하지 않음: storedFileName={}", storedFileName);
            }

        } catch (IOException e) {
            log.error("❌ 파일 삭제 실패: storedFileName={}, error={}",
                    storedFileName, e.getMessage(), e);
            throw new FileUploadException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    /**
     * 여러 파일 삭제
     */
    public void deleteFiles(List<String> storedFileNames) {

        log.info("다중 파일 삭제 시작: fileCount={}", storedFileNames.size());

        int successCount = 0;
        int failCount = 0;

        for (String fileName : storedFileNames) {
            try {
                deleteFile(fileName);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("❌ 파일 삭제 중 오류 발생: fileName={}", fileName);
            }
        }

        log.info("✅ 다중 파일 삭제 완료: totalFiles={}, successCount={}, failCount={}",
                storedFileNames.size(), successCount, failCount);
    }

    /**
     * 파일 유효성 검증
     */
    private void validateFile(MultipartFile file) {

        log.debug("파일 유효성 검증 시작: fileName={}", file.getOriginalFilename());

        // 1. 빈 파일 확인
        if (file.isEmpty()) {
            log.error("❌ 빈 파일: fileName={}", file.getOriginalFilename());
            throw new FileUploadException(ErrorCode.EMPTY_FILE);
        }

        // 2. 파일명 확인
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            log.error("❌ 잘못된 파일명: fileName=null or empty");
            throw new FileUploadException(ErrorCode.INVALID_FILE_NAME);
        }

        // 3. 확장자 검증
        String extension = getFileExtension(originalFileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.error("❌ 허용되지 않은 확장자: fileName={}, extension={}, allowedExtensions={}",
                    originalFileName, extension, ALLOWED_EXTENSIONS);
            throw new FileUploadException(ErrorCode.INVALID_FILE_TYPE);
        }

        // 4. 파일 크기 검증 (10MB)
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            log.error("❌ 파일 크기 초과: fileName={}, size={} bytes, maxSize={} bytes",
                    originalFileName, file.getSize(), maxSize);
            throw new FileUploadException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        log.debug("✅ 파일 유효성 검증 통과: fileName={}, extension={}, size={} bytes",
                originalFileName, extension, file.getSize());
    }

    /**
     * 저장될 파일명 생성
     */
    private String generateStoredFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String uuid = UUID.randomUUID().toString();
        String storedFileName = uuid + "." + extension;

        log.debug("파일명 생성: originalFileName={} -> storedFileName={}",
                originalFileName, storedFileName);

        return storedFileName;
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            log.error("❌ 확장자 없음: fileName={}", fileName);
            throw new FileUploadException(ErrorCode.INVALID_FILE_NAME);
        }
        return fileName.substring(lastDotIndex + 1);
    }
}