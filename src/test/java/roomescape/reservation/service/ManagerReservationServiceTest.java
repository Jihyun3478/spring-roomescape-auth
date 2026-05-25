package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.manager.dao.ManagerDao;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.command.UpdateReservationCommand;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservationtime.dao.ReservationTimeDao;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.shop.dao.ShopDao;
import roomescape.shop.domain.Shop;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.domain.Theme;
import roomescape.user.dao.UserDao;
import roomescape.user.domain.RoleType;
import roomescape.user.domain.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ManagerReservationServiceTest {

    @Autowired
    private ManagerReservationService managerReservationService;

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private ThemeDao themeDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ShopDao shopDao;

    @Autowired
    private ManagerDao managerDao;

    @Test
    void 자기_매장_예약_목록을_조회한다() {
        Shop shop = shopDao.insert("달빛방탈출 강남점");
        User manager = saveUser("manager@test.com", "매니저", RoleType.MANAGER);
        managerDao.insert(manager.getId(), shop.getId());
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", shop);
        saveReservation("브라운", LocalDate.now().plusDays(1), time, theme);

        List<ReservationResponse> responses = managerReservationService.getReservations(manager);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("브라운");
    }

    @Test
    void 다른_매장_예약은_조회되지_않는다() {
        Shop shop1 = shopDao.insert("달빛방탈출 강남점");
        Shop shop2 = shopDao.insert("달빛방탈출 홍대점");
        User manager = saveUser("manager@test.com", "매니저", RoleType.MANAGER);
        managerDao.insert(manager.getId(), shop1.getId());
        ReservationTime time = saveTime(10, 0);
        Theme theme2 = saveTheme("홍대테마", shop2);
        saveReservation("브라운", LocalDate.now().plusDays(1), time, theme2);

        List<ReservationResponse> responses = managerReservationService.getReservations(manager);

        assertThat(responses).isEmpty();
    }

    @Test
    void 자기_매장_예약을_변경한다() {
        Shop shop = shopDao.insert("달빛방탈출 강남점");
        User manager = saveUser("manager@test.com", "매니저", RoleType.MANAGER);
        managerDao.insert(manager.getId(), shop.getId());
        ReservationTime time1 = saveTime(10, 0);
        ReservationTime time2 = saveTime(11, 0);
        Theme theme = saveTheme("방탈출1", shop);
        Reservation saved = saveReservation("브라운", LocalDate.now().plusDays(1), time1, theme);
        UpdateReservationCommand command = new UpdateReservationCommand(
                LocalDate.now().plusDays(2), time2.getId(), manager.getId()
        );

        ReservationResponse response = managerReservationService.update(
                saved.getId(), command, manager, LocalDateTime.now()
        );

        assertThat(response.date()).isEqualTo(LocalDate.now().plusDays(2));
    }

    @Test
    void 다른_매장_예약을_변경하면_404가_발생한다() {
        Shop shop1 = shopDao.insert("달빛방탈출 강남점");
        Shop shop2 = shopDao.insert("달빛방탈출 홍대점");
        User manager = saveUser("manager@test.com", "매니저", RoleType.MANAGER);
        managerDao.insert(manager.getId(), shop1.getId());
        ReservationTime time = saveTime(10, 0);
        Theme theme2 = saveTheme("홍대테마", shop2);
        Reservation saved = saveReservation("브라운", LocalDate.now().plusDays(1), time, theme2);
        UpdateReservationCommand command = new UpdateReservationCommand(
                LocalDate.now().plusDays(2), time.getId(), manager.getId()
        );

        assertThatThrownBy(() -> managerReservationService.update(
                saved.getId(), command, manager, LocalDateTime.now()
        )).isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 자기_매장_예약을_삭제한다() {
        Shop shop = shopDao.insert("달빛방탈출 강남점");
        User manager = saveUser("manager@test.com", "매니저", RoleType.MANAGER);
        managerDao.insert(manager.getId(), shop.getId());
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", shop);
        Reservation saved = saveReservation("브라운", LocalDate.now().plusDays(1), time, theme);

        assertThatNoException().isThrownBy(
                () -> managerReservationService.delete(saved.getId(), manager)
        );
    }

    @Test
    void 다른_매장_예약을_삭제하면_404가_발생한다() {
        Shop shop1 = shopDao.insert("달빛방탈출 강남점");
        Shop shop2 = shopDao.insert("달빛방탈출 홍대점");
        User manager = saveUser("manager@test.com", "매니저", RoleType.MANAGER);
        managerDao.insert(manager.getId(), shop1.getId());
        ReservationTime time = saveTime(10, 0);
        Theme theme2 = saveTheme("홍대테마", shop2);
        Reservation saved = saveReservation("브라운", LocalDate.now().plusDays(1), time, theme2);

        assertThatThrownBy(() -> managerReservationService.delete(saved.getId(), manager))
                .isInstanceOf(RoomEscapeException.class);
    }

    private User saveUser(String email, String name, RoleType roleType) {
        return userDao.insert(User.createWithoutId(email, "password", name, roleType));
    }

    private ReservationTime saveTime(int hour, int minute) {
        return reservationTimeDao.insert(ReservationTime.createWithoutId(LocalTime.of(hour, minute)));
    }

    private Theme saveTheme(String name, Shop shop) {
        return themeDao.insert(Theme.createWithoutId(name, "설명", "https://thumb.com", shop));
    }

    private Reservation saveReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        return reservationDao.insert(Reservation.createWithoutId(name, date, time, theme, null));
    }
}
