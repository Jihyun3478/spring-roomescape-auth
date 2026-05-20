package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.UserDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.RoleType;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.request.UpdateReservationRequest;
import roomescape.dto.request.UserReservationRequest;
import roomescape.dto.response.ReservationResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private ThemeDao themeDao;

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private UserDao userDao;

    @Test
    void 예약을_추가한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        User user = saveUser("user@test.com", "password", "브라운", RoleType.MEMBER);
        UserReservationRequest request = new UserReservationRequest(LocalDate.now().plusDays(1), time.getId(), theme.getId());

        // when
        ReservationResponse response = reservationService.addReservation(request, user);

        // then
        assertThat(response)
                .extracting(ReservationResponse::name, ReservationResponse::date)
                .containsExactly("브라운", LocalDate.now().plusDays(1));
    }

    @Test
    void 존재하지_않는_시간으로_예약하면_예외가_발생한다() {
        // given
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        User user = saveUser("user@test.com", "password", "브라운", RoleType.MEMBER);
        UserReservationRequest request = new UserReservationRequest(LocalDate.of(2026, 5, 5), 999L, theme.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(request, user))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 존재하지_않는_테마로_예약하면_예외가_발생한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        User user = saveUser("user@test.com", "password", "브라운", RoleType.MEMBER);
        UserReservationRequest request = new UserReservationRequest(LocalDate.of(2026, 5, 5), time.getId(), 999L);

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(request, user))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 중복_예약을_하면_예외가_발생한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        User user = saveUser("user@test.com", "password", "브라운", RoleType.MEMBER);
        LocalDate date = LocalDate.now().plusDays(1);
        saveReservation("브라운", date, time, theme, user);

        UserReservationRequest request = new UserReservationRequest(date, time.getId(), theme.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(request, user))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 지나간_날짜로_예약하면_예외가_발생한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        User user = saveUser("user@test.com", "password", "브라운", RoleType.MEMBER);
        UserReservationRequest request = new UserReservationRequest(LocalDate.of(2026, 4, 1), time.getId(), theme.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(request, user))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 전체_예약을_조회한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        User user = saveUser("user@test.com", "password", "브라운", RoleType.MEMBER);
        saveReservation("브라운", LocalDate.of(2026, 5, 5), time, theme, user);
        saveReservation("로지", LocalDate.of(2026, 5, 6), time, theme, user);

        // when
        List<ReservationResponse> responses = reservationService.getAllReservations();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ReservationResponse::name).containsExactly("브라운", "로지");
    }

    @Test
    void 내_예약을_조회한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        User user1 = saveUser("user1@test.com", "password", "브라운", RoleType.MEMBER);
        User user2 = saveUser("user2@test.com", "password", "로지", RoleType.MEMBER);
        saveReservation("브라운", LocalDate.of(2026, 5, 5), time, theme, user1);
        saveReservation("로지", LocalDate.of(2026, 5, 6), time, theme, user2);

        // when
        List<ReservationResponse> responses = reservationService.getMyReservation(user1);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses).extracting(ReservationResponse::name).containsExactly("브라운");
    }

    @Test
    void 예약_날짜_시간을_변경한다() {
        // given
        ReservationTime time1 = saveTime(10, 0);
        ReservationTime time2 = saveTime(11, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        User user = saveUser("user@test.com", "password", "브라운", RoleType.MEMBER);
        Reservation saved = saveReservation("브라운", LocalDate.now().plusDays(1), time1, theme, user);

        UpdateReservationRequest request = new UpdateReservationRequest(LocalDate.now().plusDays(2), time2.getId());

        // when
        ReservationResponse response = reservationService.update(saved.getId(), request, user);

        // then
        assertThat(response.date()).isEqualTo(LocalDate.now().plusDays(2));
    }

    @Test
    void 존재하지_않는_예약을_변경하면_예외가_발생한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        User user = saveUser("user@test.com", "password", "브라운", RoleType.MEMBER);
        UpdateReservationRequest request = new UpdateReservationRequest(LocalDate.now().plusDays(1), time.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.update(999L, request, user))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 지나간_날짜로_변경하면_예외가_발생한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        User user = saveUser("user@test.com", "password", "브라운", RoleType.MEMBER);
        Reservation saved = saveReservation("브라운", LocalDate.now().plusDays(1), time, theme, user);

        UpdateReservationRequest request = new UpdateReservationRequest(LocalDate.of(2026, 4, 1), time.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.update(saved.getId(), request, user))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 중복된_날짜_시간으로_변경하면_예외가_발생한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        User user = saveUser("user@test.com", "password", "브라운", RoleType.MEMBER);
        LocalDate date = LocalDate.now().plusDays(1);
        saveReservation("브라운", date, time, theme, user);
        Reservation saved = saveReservation("로지", LocalDate.now().plusDays(2), time, theme, user);

        UpdateReservationRequest request = new UpdateReservationRequest(date, time.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.update(saved.getId(), request, user))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약을_삭제한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        User user = saveUser("user@test.com", "password", "브라운", RoleType.MEMBER);
        Reservation saved = saveReservation("브라운", LocalDate.of(2026, 5, 5), time, theme, user);

        // when & then
        assertThatNoException().isThrownBy(() -> reservationService.delete(saved.getId(), user));
    }

    @Test
    void 존재하지_않는_예약을_삭제하면_예외가_발생한다() {
        // given
        User user = saveUser("user@test.com", "password", "브라운", RoleType.MEMBER);

        // when & then
        assertThatThrownBy(() -> reservationService.delete(999L, user))
                .isInstanceOf(RoomEscapeException.class);
    }

    private ReservationTime saveTime(int hour, int minute) {
        return reservationTimeDao.insert(ReservationTime.createWithoutId(LocalTime.of(hour, minute)));
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        return themeDao.insert(Theme.createWithoutId(name, description, thumbnail));
    }

    private User saveUser(String email, String password, String name, RoleType roleType) {
        return userDao.insert(User.createWithoutId(email, password, name, roleType));
    }

    private Reservation saveReservation(String name, LocalDate date, ReservationTime time, Theme theme, User user) {
        return reservationDao.insert(Reservation.createWithoutId(name, date, time, theme, user));
    }
}
