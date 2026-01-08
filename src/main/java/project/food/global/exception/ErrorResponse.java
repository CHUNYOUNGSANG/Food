package project.food.global.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 에러 응답 DTO
 * 클라이언트에게 전달되는 통일된 에러 응답 형식
 */
@Getter
@Builder
public class ErrorResponse {
    private final int status;
    private final String code;
    private final String message;

    /**
     * 에러 발생 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp = LocalDateTime.now();

    /**
     * ErrorCode로부터 ErrorResponse 생성
     * @param errorCode 에러 코드
     * @return ErrorResponse 객체
     */
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    /**
     * 커스텀 메시지로 ErrorResponse 생성
     * @param errorCode 에러 코드
     * @param customMessage 커스텀 메시지
     * @return ErrorResponse 객체
     */
    public static ErrorResponse of(ErrorCode errorCode, String customMessage) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(customMessage)
                .build();
    }

}
