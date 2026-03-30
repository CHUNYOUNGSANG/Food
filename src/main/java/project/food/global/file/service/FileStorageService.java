package project.food.global.file.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.food.global.config.FileStorageConfig;
import project.food.global.exception.ErrorCode;
import project.food.global.exception.FileUploadException;
import project.food.global.file.dto.UploadedFileInfo;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class FileStorageService implements FileStorage {

    private final FileStorageConfig fileStorageConfig;
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    private static final List<String> ALLOWED_EXTENSIONS =
            List.of("jpg", "jpeg", "png", "gif", "webp");

    /**
     * 단일 파일 저장
     */
    private UploadedFileInfo storeFile(MultipartFile file, String subDir, Long memberId) {
        validateFile(file);

        String originalFileName = file.getOriginalFilename();
        String storedFileName = generateStoredFileName(originalFileName);
        String s3Key = subDir + "/" + memberId + "/" + storedFileName;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String fileUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + s3Key;

            return UploadedFileInfo.builder()
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .filePath(s3Key)
                    .fileSize(file.getSize())
                    .fileUrl(fileUrl)
                    .build();

        } catch (IOException e) {
            throw new FileUploadException(ErrorCode.FILE_UPLOAD_FAILED,
                    "파일 저장 중 오류: " + originalFileName);
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

    /**
     * 여러 파일 저장
     */
    @Override
    public List<UploadedFileInfo> storeFiles(List<MultipartFile> files, Long memberId) {

        log.info("다중 파일 저장 시작: memberId={}, fileCount={}, maxAllowed={}",
                memberId, files.size(), fileStorageConfig.getMaxFileCount());

        if (files.size() > fileStorageConfig.getMaxFileCount()) {
            log.warn("파일 개수 초과: requestCount={}, maxAllowed={}",
                    files.size(), fileStorageConfig.getMaxFileCount());
            throw new FileUploadException(ErrorCode.FILE_COUNT_EXCEEDED);
        }

        List<UploadedFileInfo> uploadedFiles = new ArrayList<>();
        int successCount = 0;
        int emptyCount = 0;

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                UploadedFileInfo fileInfo = savePostImage(file, memberId);
                uploadedFiles.add(fileInfo);
                successCount++;
            } else {
                emptyCount++;
                log.debug("빈 파일 건너뜀");
            }
        }

        log.info("다중 파일 저장 완료: memberId={}, totalFiles={}, successCount={}, emptyCount={}",
                memberId, files.size(), successCount, emptyCount);

        return uploadedFiles;
    }

    /**
     * byte[] 이미지를 S3 restaurant/ 경로에 저장 후 URL 반환
     * 네이버에서 다운로드한 이미지 저장에 사용
     */
    @Override
    public String saveRestaurantImage(byte[] imageBytes, String fileName) {
        String s3Key = "restaurant/" + UUID.randomUUID() + "_" + fileName;
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .contentType("image/jpeg")
                    .contentLength((long) imageBytes.length)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));
            return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + s3Key;
        } catch (Exception e) {
            log.error("맛집 이미지 S3 저장 실패: fileName={}, error={}", fileName, e.getMessage());
            throw new FileUploadException(ErrorCode.FILE_UPLOAD_FAILED, "맛집 이미지 저장 실패: " + fileName);
        }
    }

    /**
     * 파일 삭제 (S3 key 또는 전체 S3 URL 전달)
     */
    public void deleteFile(String s3KeyOrUrl) {

        String s3Key = s3KeyOrUrl.startsWith("https://")
                ? s3KeyOrUrl.substring(s3KeyOrUrl.indexOf(".amazonaws.com/") + ".amazonaws.com/".length())
                : s3KeyOrUrl;

        log.debug("파일 삭제 시작: s3Key={}", s3Key);

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("파일 삭제 성공: s3Key={}", s3Key);

        } catch (Exception e) {
            log.error("파일 삭제 실패: s3Key={}, error={}", s3Key, e.getMessage(), e);
            throw new FileUploadException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    /**
     * 여러 파일 삭제
     */
    public void deleteFiles(List<String> s3Keys) {

        log.info("다중 파일 삭제 시작: fileCount={}", s3Keys.size());

        int successCount = 0;
        int failCount = 0;

        for (String key : s3Keys) {
            try {
                deleteFile(key);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("파일 삭제 중 오류 발생: s3Key={}", key);
            }
        }

        log.info("다중 파일 삭제 완료: totalFiles={}, successCount={}, failCount={}",
                s3Keys.size(), successCount, failCount);
    }

    /**
     * 파일 유효성 검증
     */
    private void validateFile(MultipartFile file) {

        log.debug("파일 유효성 검증 시작: fileName={}", file.getOriginalFilename());

        if (file.isEmpty()) {
            log.error("빈 파일: fileName={}", file.getOriginalFilename());
            throw new FileUploadException(ErrorCode.EMPTY_FILE);
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            log.error("잘못된 파일명: fileName=null or empty");
            throw new FileUploadException(ErrorCode.INVALID_FILE_NAME);
        }

        String extension = getFileExtension(originalFileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.error("허용되지 않은 확장자: fileName={}, extension={}, allowedExtensions={}",
                    originalFileName, extension, ALLOWED_EXTENSIONS);
            throw new FileUploadException(ErrorCode.INVALID_FILE_TYPE);
        }

        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            log.error("파일 크기 초과: fileName={}, size={} bytes, maxSize={} bytes",
                    originalFileName, file.getSize(), maxSize);
            throw new FileUploadException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        log.debug("파일 유효성 검증 통과: fileName={}, extension={}, size={} bytes",
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
            log.error("확장자 없음: fileName={}", fileName);
            throw new FileUploadException(ErrorCode.INVALID_FILE_NAME);
        }
        return fileName.substring(lastDotIndex + 1);
    }
}
