package roomescape.common.exception.code;

import org.springframework.http.HttpStatus;

public enum ManagerErrorCode implements ErrorCode {
    NOT_FOUND("존재하지 않는 예약입니다.", HttpStatus.NOT_FOUND),
    ;

    private final String message;
    private final HttpStatus httpStatus;

    ManagerErrorCode(String message, HttpStatus httpStatus) {
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
