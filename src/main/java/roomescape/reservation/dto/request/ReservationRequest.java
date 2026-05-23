package roomescape.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.user.domain.User;

public record ReservationRequest(
        @NotBlank(message = "예약자명은 필수값 입니다.")
        @Size(max = 10, message = "예약자명은 10자 이하여야 합니다.")
        String name,

        @NotNull(message = "예약 날짜는 필수값 입니다.")
        LocalDate date,

        long timeId,
        long themeId,
        Long userId
) {
    public Reservation toReservation(ReservationTime reservationTime, Theme theme, User user) {
        return Reservation.createWithoutId(user.getName(), date, reservationTime, theme, user);
    }

    public Reservation toReservation(ReservationTime reservationTime, Theme theme) {
        return Reservation.createWithoutId(name, date, reservationTime, theme, null);
    }
}
