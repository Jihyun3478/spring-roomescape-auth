package roomescape.reservation.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.dao.ReservationTimeDao;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.shop.dao.ShopDao;
import roomescape.shop.domain.Shop;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.domain.Theme;

@JdbcTest
@Import({ReservationDao.class, ReservationTimeDao.class, ThemeDao.class, ShopDao.class})
class ReservationDaoTest {

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private ReservationTimeDao timeDao;

    @Autowired
    private ThemeDao themeDao;

    @Autowired
    private ShopDao shopDao;

    @Test
    void 예약을_생성한다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        Reservation reservation = Reservation.createWithoutId("브라운", LocalDate.of(2026, 5, 5), savedTime, savedTheme, null);

        // when
        Reservation saved = reservationDao.insert(reservation);

        // then
        assertThat(saved)
                .extracting(Reservation::getId, Reservation::getName, Reservation::getDate, Reservation::getTime,
                        Reservation::getTheme)
                .containsExactly(saved.getId(), reservation.getName(), reservation.getDate(), reservation.getTime(),
                        reservation.getTheme());
    }

    @Test
    void 예약_목록을_조회한다() {
        // given
        ReservationTime savedTime1 = saveTime(10, 0);
        ReservationTime savedTime2 = saveTime(11, 0);
        ReservationTime savedTime3 = saveTime(12, 0);
        ReservationTime savedTime4 = saveTime(13, 0);
        ReservationTime savedTime5 = saveTime(14, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        LocalDate date = LocalDate.of(2026, 5, 5);

        reservationDao.insert(Reservation.createWithoutId("브라운", date, savedTime1, savedTheme, null));
        reservationDao.insert(Reservation.createWithoutId("로지", date, savedTime2, savedTheme, null));
        reservationDao.insert(Reservation.createWithoutId("러키", date, savedTime3, savedTheme, null));
        reservationDao.insert(Reservation.createWithoutId("러로", date, savedTime4, savedTheme, null));
        reservationDao.insert(Reservation.createWithoutId("밤밤", date, savedTime5, savedTheme, null));

        // when
        List<Reservation> reservations = reservationDao.select();

        // then
        assertAll(
                () -> assertThat(reservations).hasSize(5),
                () -> assertThat(reservations.getFirst().getName()).isEqualTo("브라운")
        );
    }

    @Test
    void 특정_시간에_예약이_존재하면_true를_반환한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        reservationDao.insert(Reservation.createWithoutId("브라운", LocalDate.of(2026, 5, 5), time, theme, null));

        // when
        boolean result = reservationDao.existsByTimeId(time.getId());

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 특정_시간에_예약이_존재하지_않으면_false를_반환한다() {
        // when
        boolean result = reservationDao.existsByTimeId(999L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 특정_테마에_예약이_존재하면_true를_반환한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        reservationDao.insert(Reservation.createWithoutId("브라운", LocalDate.of(2026, 5, 5), time, theme, null));

        // when
        boolean result = reservationDao.existsByThemeId(theme.getId());

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 특정_테마에_예약이_존재하지_않으면_false를_반환한다() {
        // when
        boolean result = reservationDao.existsByThemeId(999L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 테마_아이디와_선택_날짜에_해당하는_예약_목록을_조회한다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme theme1 = saveTheme("방탈출1", "설명1", "https://asdfsdf.sdfs");
        Theme theme2 = saveTheme("방탈출2", "설명2", "https://asdfsdf.sdfs");
        LocalDate date = LocalDate.of(2026, 5, 5);

        reservationDao.insert(Reservation.createWithoutId("러키", date, savedTime, theme1, null));
        reservationDao.insert(Reservation.createWithoutId("로지", date, savedTime, theme2, null));

        // when
        List<Reservation> result = reservationDao.selectByThemeIdAndDate(theme1.getId(), date);

        // then
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.getFirst().getName()).isEqualTo("러키")
        );
    }

    @Test
    void 매장_아이디로_예약_목록을_조회한다() {
        // given
        Shop shop1 = shopDao.insert("달빛방탈출 강남점");
        Shop shop2 = shopDao.insert("달빛방탈출 홍대점");
        ReservationTime time = saveTime(10, 0);
        Theme theme1 = saveTheme("방탈출1", "설명", "https://thumb.com", shop1);
        Theme theme2 = saveTheme("방탈출2", "설명", "https://thumb.com", shop2);
        reservationDao.insert(Reservation.createWithoutId("브라운", LocalDate.of(2026, 5, 5), time, theme1, null));
        reservationDao.insert(Reservation.createWithoutId("로지", LocalDate.of(2026, 5, 6), time, theme2, null));

        // when
        List<Reservation> result = reservationDao.selectByShopId(shop1.getId());

        // then
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.getFirst().getName()).isEqualTo("브라운")
        );
    }

    @Test
    void 예약_아이디와_매장_아이디로_예약을_조회한다() {
        // given
        Shop shop = shopDao.insert("달빛방탈출 강남점");
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com", shop);
        Reservation saved = reservationDao.insert(
                Reservation.createWithoutId("브라운", LocalDate.of(2026, 5, 5), time, theme, null));

        // when
        Optional<Reservation> found = reservationDao.selectByIdAndShopId(saved.getId(), shop.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("브라운");
    }

    @Test
    void 다른_매장_아이디로_조회하면_빈_객체를_반환한다() {
        // given
        Shop shop1 = shopDao.insert("달빛방탈출 강남점");
        Shop shop2 = shopDao.insert("달빛방탈출 홍대점");
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com", shop1);
        Reservation saved = reservationDao.insert(
                Reservation.createWithoutId("브라운", LocalDate.of(2026, 5, 5), time, theme, null));

        // when
        Optional<Reservation> found = reservationDao.selectByIdAndShopId(saved.getId(), shop2.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 날짜_시간_테마가_모두_같은_예약이_존재하면_true를_반환한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 5, 5);
        reservationDao.insert(Reservation.createWithoutId("브라운", date, time, theme, null));

        // when
        boolean result = reservationDao.existsByDateAndTimeIdAndThemeId(date, time.getId(), theme.getId());

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 날짜_시간_테마가_모두_같은_예약이_없으면_false를_반환한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 5, 5);

        // when
        boolean result = reservationDao.existsByDateAndTimeIdAndThemeId(date, time.getId(), theme.getId());

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 예약을_수정한다() {
        // given
        ReservationTime time1 = saveTime(10, 0);
        ReservationTime time2 = saveTime(11, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        Reservation saved = reservationDao.insert(
                Reservation.createWithoutId("브라운", LocalDate.of(2026, 5, 5), time1, theme, null));

        // when
        Reservation updated = reservationDao.update(saved.getId(), LocalDate.of(2026, 5, 6), time2.getId());

        // then
        assertAll(
                () -> assertThat(updated.getDate()).isEqualTo(LocalDate.of(2026, 5, 6)),
                () -> assertThat(updated.getTime().getId()).isEqualTo(time2.getId())
        );
    }

    @Test
    void 예약을_삭제한다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        Reservation saved = reservationDao.insert(
                Reservation.createWithoutId("예약1", LocalDate.of(2026, 5, 5), savedTime, savedTheme, null));

        // when
        reservationDao.delete(saved.getId());

        // then
        assertThat(reservationDao.select()).isEmpty();
    }

    private ReservationTime saveTime(int hour, int minute) {
        return timeDao.insert(ReservationTime.createWithoutId(LocalTime.of(hour, minute)));
    }

    private Theme saveTheme(String name, String description, String thumbnail, Shop shop) {
        return themeDao.insert(Theme.createWithoutId(name, description, thumbnail, shop));
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        return themeDao.insert(Theme.createWithoutId(name, description, thumbnail, null));
    }
}
