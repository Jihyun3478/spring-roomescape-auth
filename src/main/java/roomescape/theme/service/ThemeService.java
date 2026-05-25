package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.ShopErrorCode;
import roomescape.common.exception.code.ThemeErrorCode;
import roomescape.reservation.dao.ReservationDao;
import roomescape.shop.dao.ShopDao;
import roomescape.shop.domain.Shop;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.command.ThemeCommand;
import roomescape.theme.dto.response.ThemeResponse;

@Service
@Transactional
public class ThemeService {
    private static final int POPULAR_THEME_PERIOD_DAYS = 6;

    private final ThemeDao themeDao;
    private final ReservationDao reservationDao;
    private final ShopDao shopDao;

    public ThemeService(ThemeDao themeDao, ReservationDao reservationDao, ShopDao shopDao) {
        this.themeDao = themeDao;
        this.reservationDao = reservationDao;
        this.shopDao = shopDao;
    }

    public ThemeResponse addTheme(ThemeCommand command) {
        validateUniqueTheme(command.name());

        Shop shop = null;
        if (Objects.nonNull(command.shopId())) {
            shop = shopDao.selectById(command.shopId())
                    .orElseThrow(() -> new RoomEscapeException(ShopErrorCode.NOT_FOUND));
        }

        Theme theme = Theme.createWithoutId(command.name(), command.description(), command.thumbnail(), shop);
        Theme savedTheme = themeDao.insert(theme);
        return ThemeResponse.from(savedTheme);
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> getThemes() {
        return themeDao.selectAll().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> getPopularThemes(LocalDate today) {
        LocalDate startDate = today.minusDays(POPULAR_THEME_PERIOD_DAYS);
        LocalDate endDate = today.minusDays(1);

        List<Theme> popularThemes = themeDao.selectPopularThemesByPeriod(startDate, endDate);
        return popularThemes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public void deleteTheme(long themeId) {
        Optional<Theme> theme = themeDao.selectById(themeId);
        if (theme.isEmpty()) {
            throw new RoomEscapeException(ThemeErrorCode.NOT_FOUND);
        }

        validateThemeIncludeReservation(themeId);
        themeDao.delete(themeId);
    }

    private void validateUniqueTheme(String name) {
        boolean exists = themeDao.existsByName(name);
        if (exists) {
            throw new RoomEscapeException(ThemeErrorCode.DUPLICATE);
        }
    }

    private void validateThemeIncludeReservation(long themeId) {
        boolean existsByThemeId = reservationDao.existsByThemeId(themeId);
        if (existsByThemeId) {
            throw new RoomEscapeException(ThemeErrorCode.THEME_CANNOT_DELETE);
        }
    }
}
