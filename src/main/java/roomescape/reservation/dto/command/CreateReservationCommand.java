package roomescape.reservation.dto.command;

import java.time.LocalDate;

public record CreateReservationCommand(
        LocalDate date,
        long timeId,
        long themeId,
        Long userId
) {
}
