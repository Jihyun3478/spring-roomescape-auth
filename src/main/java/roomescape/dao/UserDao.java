package roomescape.dao;

import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.RoleType;
import roomescape.domain.User;

@Repository
public class UserDao {
    private static final RowMapper<User> ROW_MAPPER = (resultSet, rowNum) -> new User(
            resultSet.getLong("id"),
            resultSet.getString("email"),
            resultSet.getString("password"),
            resultSet.getString("name"),
            RoleType.valueOf(resultSet.getString("role"))
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<User> findByEmail(String email) {
        try {
            String sql = """
                    SELECT id, email, password, name, role
                    FROM user
                    WHERE email = ?""";
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, email));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<User> findById(long id) {
        try {
            String sql = """
                    SELECT id, email, password, name, role
                    FROM user
                    WHERE id = ?""";
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }
}
