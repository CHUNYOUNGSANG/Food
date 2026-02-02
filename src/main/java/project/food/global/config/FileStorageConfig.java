package project.food.global.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * 파일 저장 설정
 * - application.yml의 파일 업로드 설정을 관리
 */
@Slf4j
@Configuration
@Getter
public class FileStorageConfig {

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Value("${file.upload.max-file-count}")
    private int maxFileCount;

    @PostConstruct  // 스프링 빈 초기화 후 자동 실행
    public void init() {
        log.info("파일 스토리지 설정 초기화: uploadDir={}, maxFileCount={}",
                uploadDir, maxFileCount);

        File uploadDirectory = new File(uploadDir);

        if (!uploadDirectory.exists()) {
            boolean created = uploadDirectory.mkdirs();
            if (created) {
                log.info("✅ 업로드 디렉토리 생성 완료: {}", uploadDir);
            } else {
                log.error("❌ 업로드 디렉토리 생성 실패: {}", uploadDir);
            }
        } else {
            log.info("✅ 업로드 디렉토리 존재 확인: {}", uploadDir);
        }
    }
}
