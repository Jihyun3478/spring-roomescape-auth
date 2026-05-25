package roomescape.reservation.dao;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.shop.domain.Shop;
import roomescape.user.domain.RoleType;
import roomescape.theme.domain.Theme;
import roomescape.user.domain.User;

@Repository
public class ReservationDao {
    private static final RowMapper<Reservation> ROW_MAPPER = (resultSet, rowNum) -> {
        Shop shop = new Shop(
                resultSet.getLong("id"),
                resultSet.getString("name")
        );

        ReservationTime reservationTime = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );

        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail"),
                shop
        );

        long userId = resultSet.getLong("user_id");
        User user = null;
        if (userId != 0) {
            user = new User(
                    userId,
                    resultSet.getString("email"),
                    resultSet.getString("password"),
                    resultSet.getString("user_name"),
                    RoleType.valueOf(resultSet.getString("role"))
            );
        }

        return new Reservation(
                resultSet.getLong("id"),
                resultSet.getString("reservation_name"),
                resultSet.getDate("date").toLocalDate(),
                reservationTime,
                theme,
                user
        );
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public Reservation insert(Reservation reservation) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", reservation.getName());
        parameters.put("date", reservation.getDate());
        parameters.put("time_id", reservation.getTime().getId());
        parameters.put("theme_id", reservation.getTheme().getId());
        User user = reservation.getUser();
        if (Objects.nonNull(user)) {
            parameters.put("user_id", user.getId());
        }

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);
        return new Reservation(
                generatedId.longValue(),
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                reservation.getUser()
        );
    }

    public List<Reservation> select() {
        return jdbcTemplate.query(baseSelectSql(), ROW_MAPPER);
    }

    public Optional<Reservation> selectById(long reservationId) {
        try {
            String sql = baseSelectSql() + " WHERE r.id = ?";
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, reservationId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<Reservation> selectByUserId(long userId) {
        String sql = baseSelectSql() + " WHERE r.user_id = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, userId);
    }

    public List<Reservation> selectByThemeIdAndDate(long themeId, LocalDate date) {
        String sql = baseSelectSql() + " WHERE r.theme_id = ? AND r.date = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, themeId, date);
    }

    public List<Reservation> selectByShopId(long shopId) {
        String sql = baseSelectSql() + " WHERE s.id = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, shopId);
    }

    public Optional<Reservation> selectByIdAndShopId(long reservationId, long shopId) {
        try {
            String sql = baseSelectSql() + " WHERE r.id = ? AND s.id = ?";
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, reservationId, shopId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public boolean existsByTimeId(long timeId) {
        String sql = "SELECT COUNT(*) > 0 FROM reservation WHERE time_id = ?";
        return jdbcTemplate.queryForObject(sql, Boolean.class, timeId);
    }

    public boolean existsByThemeId(long themeId) {
        String sql = "SELECT COUNT(*) > 0 FROM reservation WHERE theme_id = ?";
        return jdbcTemplate.queryForObject(sql, Boolean.class, themeId);
    }

    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId) {
        String sql = """
                SELECT COUNT(*) > 0
                FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ?""";
        return jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId);
    }

    public boolean existsDuplicateExcluding(LocalDate date, long timeId, long themeId, long reservationId) {
        String sql = """
                SELECT COUNT(*) > 0
                FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ? AND id != ?""";
        return jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId, reservationId);
    }

    public Reservation update(Long reservationId, LocalDate date, long timeId) {
        String sql = "UPDATE reservation SET date = ?, time_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, date, timeId, reservationId);
        return selectById(reservationId).get();
    }

    public int delete(Long reservationId) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        return jdbcTemplate.update(sql, reservationId);
    }

    private String baseSelectSql() {
        return """
            SELECT r.id,
                   r.name as reservation_name,
                   r.date,
                   rt.id as time_id,
                   rt.start_at,
                   t.id as theme_id,
                   t.name as theme_name,
                   t.description,
                   t.thumbnail,
                   s.id as shop_id,
                   s.name as shop_name,
                   u.id as user_id,
                   u.email,
                   u.password,
                   u.name as user_name,
                   u.role
            FROM reservation AS r
            INNER JOIN reservation_time AS rt ON r.time_id = rt.id
            INNER JOIN theme AS t ON r.theme_id = t.id
            LEFT JOIN shop AS s ON t.shop_id = s.id
            LEFT JOIN users AS u ON r.user_id = u.id
            """;
    }
}
