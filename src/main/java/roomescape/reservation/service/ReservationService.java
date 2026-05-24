package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.ReservationErrorCode;
import roomescape.common.exception.code.ReservationTimeErrorCode;
import roomescape.common.exception.code.ThemeErrorCode;
import roomescape.common.exception.code.UserErrorCode;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.command.CreateAdminReservationCommand;
import roomescape.reservation.dto.command.CreateReservationCommand;
import roomescape.reservation.dto.command.UpdateReservationCommand;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservationtime.dao.ReservationTimeDao;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.domain.Theme;
import roomescape.user.dao.UserDao;
import roomescape.user.domain.User;

@Service
@Transactional
public class ReservationService {
    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final UserDao userDao;

    public ReservationService(ReservationDao reservationDao, ReservationTimeDao reservationTimeDao, ThemeDao themeDao,
                              UserDao userDao) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.userDao = userDao;
    }

    public ReservationResponse addReservation(CreateReservationCommand command, LocalDateTime now) {
        ReservationTime reservationTime = getTime(command.timeId());
        Theme theme = getTheme(command.themeId());

        validateUniqueReservation(command.date(), command.timeId(), command.themeId());
        validatePastDatetime(command.date(), reservationTime, now);

        User user = userDao.selectById(command.userId())
                .orElseThrow(() -> new RoomEscapeException(UserErrorCode.NOT_FOUND));

        Reservation reservation = Reservation.createWithoutId(user.getName(), command.date(), reservationTime, theme, user);
        Reservation savedReservation = reservationDao.insert(reservation);
        return ReservationResponse.from(savedReservation);
    }

    public ReservationResponse addReservationByAdmin(CreateAdminReservationCommand command, LocalDateTime now) {
        ReservationTime reservationTime = getTime(command.timeId());
        Theme theme = getTheme(command.themeId());

        validateUniqueReservation(command.date(), command.timeId(), command.themeId());
        validatePastDatetime(command.date(), reservationTime, now);

        User user = null;
        if (Objects.nonNull(command.userId())) {
            user = userDao.selectById(command.userId())
                    .orElseThrow(() -> new RoomEscapeException(UserErrorCode.NOT_FOUND));
        }

        Reservation reservation = Reservation.createWithoutId(command.name(), command.date(), reservationTime, theme,
                user);
        Reservation savedReservation = reservationDao.insert(reservation);
        return ReservationResponse.from(savedReservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        return reservationDao.select().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservation(User user) {
        return reservationDao.selectByUserId(user.getId()).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse update(Long reservationId, UpdateReservationCommand command, LocalDateTime now) {
        Reservation reservation = getReservation(reservationId);
        if (Objects.isNull(reservation.getUser()) || !reservation.getUser().getId().equals(command.userId())) {
            throw new RoomEscapeException(ReservationErrorCode.UNAUTHORIZED_ACCESS);
        }

        ReservationTime time = getTime(command.timeId());
        validateUniqueExcludingSelf(command.date(), command.timeId(), reservation.getTheme().getId(), reservation.getId());
        validatePastDatetime(command.date(), time, now);

        Reservation updateReservation = reservationDao.update(reservationId, command.date(), command.timeId());
        return ReservationResponse.from(updateReservation);
    }

    public void delete(Long reservationId, User user) {
        Reservation reservation = getReservation(reservationId);
        if (Objects.isNull(reservation.getUser()) || !reservation.getUser().getId().equals(user.getId())) {
            throw new RoomEscapeException(ReservationErrorCode.UNAUTHORIZED_ACCESS);
        }

        int deleted = reservationDao.delete(reservationId);
        if (deleted == 0) {
            throw new RoomEscapeException(ReservationErrorCode.NOT_FOUND);
        }
    }

    public void deleteByAdmin(Long reservationId) {
        int deleted = reservationDao.delete(reservationId);
        if (deleted == 0) {
            throw new RoomEscapeException(ReservationErrorCode.NOT_FOUND);
        }
    }

    private ReservationTime getTime(long timeId) {
        return reservationTimeDao.selectById(timeId)
                .orElseThrow(() -> new RoomEscapeException(ReservationTimeErrorCode.NOT_FOUND));
    }

    private Theme getTheme(long themeId) {
        return themeDao.selectById(themeId)
                .orElseThrow(() -> new RoomEscapeException(ThemeErrorCode.NOT_FOUND));
    }

    private void validateUniqueReservation(LocalDate date, long timeId, long themeId) {
        boolean exists = reservationDao.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
        if (exists) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE);
        }
    }

    private void validateUniqueExcludingSelf(LocalDate date, long timeId, long themeId, long id) {
        boolean exists = reservationDao.existsDuplicateExcluding(date, timeId, themeId, id);
        if (exists) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE);
        }
    }

    private void validatePastDatetime(LocalDate date, ReservationTime reservationTime, LocalDateTime now) {
        LocalDateTime reservationDateAndTime = LocalDateTime.of(date, reservationTime.getStartAt());
        if (reservationDateAndTime.isBefore(now)) {
            throw new RoomEscapeException(ReservationErrorCode.PAST_DATETIME);
        }
    }

    private Reservation getReservation(Long reservationId) {
        return reservationDao.selectById(reservationId)
                .orElseThrow(() -> new RoomEscapeException(ReservationErrorCode.NOT_FOUND));
    }
}
