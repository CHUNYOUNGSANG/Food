package project.food.global.exception;

import lombok.Getter;

/**
 * 파일 업로드 관련 예외
 */
@Getter
public class FileUploadException extends CustomException {

    public FileUploadException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FileUploadException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

}
