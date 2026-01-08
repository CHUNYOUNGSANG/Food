package project.food.global.exception;

import lombok.Getter;

/**
 * 커스텀 예외 기본 클래스
 * 모든 비즈니스 예외는 이 클래스를 상속받음
 * ErrorCode를 통해 일관된 예외 처리
 */

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * ErrorCode로 예외 생성
     * @param errorCode 에러 코드
     */
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode와 커스텀 메시지로 예외 설정
     * @param errorCode 에러 코드
     * @param customMessage 커스텀 메시지
     */
    public CustomException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
