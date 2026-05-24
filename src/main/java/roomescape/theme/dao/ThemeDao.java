package roomescape.theme.dao;

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
import roomescape.shop.domain.Shop;
import roomescape.theme.domain.Theme;

@Repository
public class ThemeDao {
    private static final RowMapper<Theme> ROW_MAPPER = (resultSet, rowNum) -> {
        Shop shop = null;
        long shopId = resultSet.getLong("shop_id");
        if (shopId != 0) {
            shop = new Shop(
                    shopId,
                    resultSet.getString("shop_name")
            );
        }

        return new Theme(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail"),
                shop
        );
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    public Theme insert(Theme theme) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", theme.getName());
        parameters.put("description", theme.getDescription());
        parameters.put("thumbnail", theme.getThumbnail());
        if (Objects.nonNull(theme.getShop())) {
            parameters.put("shop_id", theme.getShop().getId());
        }

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);
        return new Theme(
                generatedId.longValue(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail(),
                theme.getShop()
        );
    }

    public Optional<Theme> selectById(Long themeId) {
        String sql = """
                SELECT t.id, 
                       t.name, 
                       t.description,
                       t.thumbnail,
                       s.id as shop_id,
                       s.name as shop_name
                FROM theme t
                LEFT JOIN shop s ON t.shop_id = s.id
                WHERE t.id = ?""";

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, themeId));
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        }
    }

    public List<Theme> selectAll() {
        String sql = """
               
                SELECT t.id,
                       t.name,
                       t.description,
                       t.thumbnail,
                       s.id as shop_id,
                       s.name as shop_name
                FROM theme t
                LEFT JOIN shop s ON t.shop_id = s.id
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public List<Theme> selectPopularThemesByPeriod(LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT t.id,
                       t.name,
                       t.description,
                       t.thumbnail,
                       s.id as shop_id,
                       s.name as shop_name
                FROM reservation AS r
                INNER JOIN theme AS t ON r.theme_id = t.id
                    LEFT JOIN shop s ON t.shop_id = s.id
                WHERE r.date BETWEEN ? AND ?
                GROUP BY t.id, t.name, t.description, t.thumbnail, s.id, s.name
                ORDER BY COUNT(r.id) DESC
                LIMIT 10
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, startDate, endDate);
    }

    public boolean existsById(Long themeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM theme
                    WHERE id = ?
                )
                """;

        return jdbcTemplate.queryForObject(sql, boolean.class, themeId);
    }

    public boolean existsByName(String name) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM theme
                    WHERE name = ?
                )
                """;

        return jdbcTemplate.queryForObject(sql, boolean.class, name);
    }

    public int delete(long themeId) {
        String sql = """
                DELETE FROM theme
                WHERE id = ?""";
        return jdbcTemplate.update(sql, themeId);
    }
}
