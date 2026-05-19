package roomescape.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UserReservationRequest(
        @NotNull(message = "예약 날짜는 필수값 입니다.")
        LocalDate date,

        long timeId,
        long themeId
) {
}
