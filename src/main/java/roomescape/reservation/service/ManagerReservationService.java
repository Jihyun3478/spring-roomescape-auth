package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.ManagerErrorCode;
import roomescape.common.exception.code.ReservationErrorCode;
import roomescape.common.exception.code.ReservationTimeErrorCode;
import roomescape.manager.dao.ManagerDao;
import roomescape.manager.domain.Manager;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.command.UpdateReservationCommand;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservationtime.dao.ReservationTimeDao;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.user.domain.User;

@Service
@Transactional
public class ManagerReservationService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ManagerDao managerDao;

    public ManagerReservationService(ReservationDao reservationDao, ReservationTimeDao reservationTimeDao,
                                     ManagerDao managerDao) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.managerDao = managerDao;
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservations(User user) {
        Manager manager = getManager(user);
        return reservationDao.selectByShopId(manager.getShop().getId()).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse update(Long reservationId, UpdateReservationCommand command, User user, LocalDateTime now) {
        Manager manager = getManager(user);
        Reservation reservation = reservationDao.selectByIdAndShopId(reservationId, manager.getShop().getId())
                .orElseThrow(() -> new RoomEscapeException(ManagerErrorCode.NOT_FOUND));

        ReservationTime time = reservationTimeDao.selectById(command.timeId())
                .orElseThrow(() -> new RoomEscapeException(ReservationTimeErrorCode.NOT_FOUND));

        validatePastDatetime(command.date(), time, now);
        validateUniqueExcludingSelf(command.date(), command.timeId(),
                reservation.getTheme().getId(), reservationId);

        Reservation updateReservation = reservationDao.update(reservationId, command.date(), command.timeId());
        return ReservationResponse.from(updateReservation);
    }

    public void delete(Long reservationId, User user) {
        Manager manager = getManager(user);
        reservationDao.selectByIdAndShopId(reservationId, manager.getShop().getId())
                .orElseThrow(() -> new RoomEscapeException(ManagerErrorCode.NOT_FOUND));

        reservationDao.delete(reservationId);
    }

    private Manager getManager(User user) {
        return managerDao.selectByUserId(user.getId())
                .orElseThrow(() -> new RoomEscapeException(ManagerErrorCode.NOT_FOUND));
    }

    private void validatePastDatetime(LocalDate date, ReservationTime time, LocalDateTime now) {
        LocalDateTime reservationDateAndTime = LocalDateTime.of(date, time.getStartAt());
        if (reservationDateAndTime.isBefore(now)) {
            throw new RoomEscapeException(ReservationErrorCode.PAST_DATETIME);
        }
    }

    private void validateUniqueExcludingSelf(LocalDate date, long timeId, long themeId, long reservationId) {
        boolean exists = reservationDao.existsDuplicateExcluding(date, timeId, themeId, reservationId);
        if (exists) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE);
        }
    }
}
