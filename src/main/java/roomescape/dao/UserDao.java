package roomescape.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
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

    public User insert(User user) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", user.getEmail());
        parameters.put("password", user.getPassword());
        parameters.put("name", user.getName());
        parameters.put("role", user.getRoleType().name());

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);
        return new User(generatedId.longValue(), user.getEmail(), user.getPassword(), user.getName(), user.getRoleType());
    }

    public Optional<User> findByEmail(String email) {
        try {
            String sql = """
                    SELECT id, email, password, name, role
                    FROM users
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
                    FROM users
                    WHERE id = ?""";
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }
}
