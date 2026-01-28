package project.food.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * 파일 저장 설정
 * - application.yml의 파일 업로드 설정을 관리
 */
@Configuration
@Getter
public class FileStorageConfig {

    /**
     * 파일 저장 디렉토리 경로
     */
    @Value("${file.upload.dir}")
    private String uploadDir;

    /**
     * 게시글당 최대 파일 개수
     */
    @Value("${file.upload.max-file-count}")
    private int maxFileCount;

    public void init() {
        File uploadDirectory = new File(uploadDir);

        if (!uploadDirectory.exists()) {
            boolean created = uploadDirectory.mkdirs();
            if (created) {
                System.out.println("✅ 업로드 디렉토리 생성 완료:" + uploadDir);
            } else {
                System.err.println("❌ 업로드 디렉토리 생성 실패:" + uploadDir);
            }
        }

    }

}
