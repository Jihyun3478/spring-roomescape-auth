package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.auth.LoginUser;
import roomescape.reservation.dto.command.CreateReservationCommand;
import roomescape.reservation.dto.command.UpdateReservationCommand;
import roomescape.reservation.dto.request.UpdateReservationRequest;
import roomescape.reservation.dto.request.UserReservationRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.user.domain.User;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private static final String LOCATION_DEFAULT_VALUE = "/reservations/";

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(
            @Valid @RequestBody UserReservationRequest request,
            @LoginUser User user) {
        CreateReservationCommand command = new CreateReservationCommand(
                request.date(), request.timeId(), request.themeId(), user.getId()
        );
        ReservationResponse response = reservationService.addReservation(command, LocalDateTime.now());
        return ResponseEntity.created(URI.create(LOCATION_DEFAULT_VALUE + response.id()))
                .body(response);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(@LoginUser User user) {
        List<ReservationResponse> responses = reservationService.getMyReservation(user);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable("reservationId") Long reservationId,
            @RequestBody UpdateReservationRequest request,
            @LoginUser User user) {
        UpdateReservationCommand command = new UpdateReservationCommand(
                request.date(), request.timeId(), user.getId()
        );
        ReservationResponse response = reservationService.update(reservationId, command, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("reservationId") Long reservationId,
                                                  @LoginUser User user) {
        reservationService.delete(reservationId, user);
        return ResponseEntity.noContent().build();
    }
}
