package project.food.global.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadedFileInfo {

    /**
     * 원본 파일명
     */
    private String originalFileName;

    /**
     * 저장된 파일명 (UUID로 생성된 고유 이름)
     */
    private String storedFileName;

    /**
     * 파일이 저장된 전체 경로
     */
    private String filePath;

    /**
     * 파일 크기 (bytes)
     */
    private Long fileSize;

    /**
     * 파일 접근 URL (나중에 웹에서 접근할 때 사용)
     */
    private String fileUrl;

}
