package roomescape.common.exception.code;

import org.springframework.http.HttpStatus;

public enum ShopErrorCode implements ErrorCode {
    NOT_FOUND("존재하지 않는 매장입니다.", HttpStatus.NOT_FOUND),
    ;

    private final String message;
    private final HttpStatus httpStatus;

    ShopErrorCode(String message, HttpStatus httpStatus) {
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
