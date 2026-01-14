package project.food.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 핸들러
 * 모든 Controller에서 발생하는 예외를 한 곳에서 처리
 * 통일된 에러 응답 형식 제공
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * CustomException 처리
     * 프로젝트의 모든 비즈니스 예외를 여기서 처리
     * @param e CustomException
     * @return ErrorResponse
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("CustomException 발생: [{}] {}", e.getErrorCode().getCode(), e.getMessage());

        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = ErrorResponse.of(errorCode);

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    /**
     * Validation 에외 처리
     * @Valid 검증 실패 시 발생
     * @param e MethodArgumentNotValidException
     * @return ErrorResponse
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation Exception 발생: {}", e.getMessage());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE,
                e.getBindingResult().getAllErrors().get(0).getDefaultMessage()
        );

        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus()).body(response);
    }

    /**
     * HTTP Method 불일치 예외 처리
     * GET 요청이 필요한데 POST로 요청한 경우 등
     * @param e HttpRequestMethodNotSupportedException
     * @return ErrorResponse
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(HttpMediaTypeNotSupportedException e) {
        log.error("Method Not Allowed Exception 발생: {}", e.getMessage());

        ErrorResponse response = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED);

        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getStatus()).body(response);
    }

    /**
     * 예상치 못한 모든 예외 처리
     * @param e Exception
     * @return ErrorResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected Exception 발생: ", e);

        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(response);
    }

}
