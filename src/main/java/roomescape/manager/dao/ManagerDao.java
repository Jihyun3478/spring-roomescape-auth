package roomescape.manager.dao;

import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.manager.domain.Manager;
import roomescape.shop.domain.Shop;
import roomescape.user.domain.RoleType;
import roomescape.user.domain.User;

@Repository
public class ManagerDao {
    private static final RowMapper<Manager> ROW_MAPPER = (resultSet, rowNum) -> {
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

        Shop shop = new Shop(
                resultSet.getLong("shop_id"),
                resultSet.getString("shop_name")
        );

        return new Manager(
                resultSet.getLong("id"),
                user,
                shop
        );
    };

    private final JdbcTemplate jdbcTemplate;

    public ManagerDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Manager> selectByUserId(long userId) {
        try {
            String sql = baseSelectSql() + " WHERE m.user_id = ?";
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, userId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private String baseSelectSql() {
        return """
                SELECT m.id,
                       s.id as shop_id,
                       s.name as shop_name,
                       u.id as user_id,
                       u.email,
                       u.password,
                       u.name as user_name,
                       u.role
                FROM manager AS m
                INNER JOIN shop AS s ON m.shop_id = s.id
                INNER JOIN users AS u ON m.user_id = u.id
                """;
    }
}
