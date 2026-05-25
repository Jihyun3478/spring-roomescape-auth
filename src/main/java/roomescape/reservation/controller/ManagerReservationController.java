package roomescape.reservation.controller;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.auth.LoginUser;
import roomescape.reservation.dto.command.UpdateReservationCommand;
import roomescape.reservation.dto.request.UpdateReservationRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.ManagerReservationService;
import roomescape.user.domain.User;

@RestController
@RequestMapping("/manager/reservations")
public class ManagerReservationController {

    private final ManagerReservationService managerReservationService;

    public ManagerReservationController(ManagerReservationService managerReservationService) {
        this.managerReservationService = managerReservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservations(@LoginUser User user) {
        List<ReservationResponse> responses = managerReservationService.getReservations(user);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable("reservationId") Long reservationId,
            @RequestBody UpdateReservationRequest request,
            @LoginUser User user
    ) {
        UpdateReservationCommand command = new UpdateReservationCommand(
                request.date(), request.timeId(), user.getId()
        );
        ReservationResponse response = managerReservationService.update(
                reservationId, command, user, LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable("reservationId") Long reservationId,
            @LoginUser User user
    ) {
        managerReservationService.delete(reservationId, user);
        return ResponseEntity.noContent().build();
    }
}
