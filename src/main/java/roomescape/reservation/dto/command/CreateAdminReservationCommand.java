package roomescape.reservation.dto.command;

import java.time.LocalDate;

public record CreateAdminReservationCommand(
        String name,
        LocalDate date,
        long timeId,
        long themeId,
        Long userId
) {}
