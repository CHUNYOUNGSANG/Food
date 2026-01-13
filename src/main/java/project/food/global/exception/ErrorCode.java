package project.food.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 * 프로젝트의 모든 에러를 한 곳에 관리
 * HTTP 상태 코드, 에러 코드, 메시지를 함께 정의
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /** 공통 에러 (C: Common) */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C002", "입력값이 올바르지 않습니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C003", "지원하지 않는 HTTP 메서드입니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C004", "인증이 필요합니다"),

    /** 회원 관련 에러 (M: Member) */
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원을 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "M002", "이미 존재하는 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "M003", "이미 존재하는 닉네임입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "M004", "비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "M005", "접근 권한이 없습니다."),

    /** 게시글 관련 에러 (P: Post) */
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "게시글을 찾을 수 없습니다"),
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "P002", "게시글에 대한 권한이 없습니다"),
    INVALID_POST_CONTENT(HttpStatus.BAD_REQUEST, "P003", "게시글 내용이 올바르지 않습니다"),

    /** 파일 관련 에러 (F: File) */
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F001", "파일 업로드에 실패했습니다"),
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "F002", "지원하지 않는 파일 형식입니다"),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "F003", "파일 크기가 너무 큽니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
