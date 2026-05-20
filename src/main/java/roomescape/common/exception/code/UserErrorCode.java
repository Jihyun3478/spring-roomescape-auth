package roomescape.common.exception.code;

import org.springframework.http.HttpStatus;

public enum UserErrorCode implements ErrorCode {
    NOT_FOUND("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED("인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    LOGIN_FAIL("이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    ;

    private final String message;
    private final HttpStatus httpStatus;

    UserErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
