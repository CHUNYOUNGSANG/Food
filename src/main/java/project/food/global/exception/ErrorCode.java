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

    /** 공통 에러 (G: Global) */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "G002", "입력값이 올바르지 않습니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "G003", "지원하지 않는 HTTP 메서드입니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "G004", "인증이 필요합니다"),

    /** 회원 관련 에러 (M: Member) */
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원을 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "M002", "이미 존재하는 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "M003", "이미 존재하는 닉네임입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "M004", "비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "M005", "접근 권한이 없습니다."),

    /** 게시글 관련 에러 (P: Post) */
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "게시글을 찾을 수 없습니다."),
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "P002", "게시글에 대한 권한이 없습니다."),
    INVALID_POST_CONTENT(HttpStatus.BAD_REQUEST, "P003", "게시글 내용이 올바르지 않습니다."),

    /** 댓글 관련 에러 (C: Comment) */
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "C001", "댓글을 찾을 수 없습니다."),
    COMMENT_AUTHOR_MISMATCH(HttpStatus.FORBIDDEN, "C002", "댓글 작성자만 수정/삭제할 수 있습니다."),
    COMMENT_HAS_REPLIES(HttpStatus.BAD_REQUEST, "C003", "대댓글이 있는 댓글은 삭제할 수 없습니다."),
    INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "C004", "유효하지 않은 부모 댓글입니다."),
    COMMENT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "C005", "이미 삭제된 댓글입니다."),

    /** 파일 관련 에러 (F: File) */
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F001", "파일 업로드에 실패했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "F002", "파일을 찾을 수 없습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "F003", "허용되지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "F004", "파일 크기가 제한을 초과했습니다."),
    FILE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "F005", "업로드 가능한 파일 개수를 초과했습니다."),
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "F006", "빈 파일은 업로드할 수 없습니다."),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "F007", "파일명이 올바르지 않습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F008", "파일 삭제에 실패했습니다."),

    /** 좋아요 관련 에러 (L: Like) */
    ALREADY_LIKED(HttpStatus.CONFLICT, "L001", "이미 좋아요를 눌렀습니다."),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "L002", "좋아요를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
