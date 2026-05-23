package roomescape;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import roomescape.user.dao.UserDao;
import roomescape.user.domain.RoleType;
import roomescape.user.domain.User;

@Component
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;
    private final UserDao userDao;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate, UserDao userDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDao = userDao;
    }

    public void clear() {
        jdbcTemplate.update("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.update("TRUNCATE TABLE users");
        jdbcTemplate.update("TRUNCATE TABLE reservation");
        jdbcTemplate.update("TRUNCATE TABLE reservation_time");
        jdbcTemplate.update("TRUNCATE TABLE theme");
        jdbcTemplate.update("SET REFERENTIAL_INTEGRITY TRUE");
    }

    public void insertDefaultUsers() {
        userDao.insert(User.createWithoutId("admin@example.com", "password", "관리자", RoleType.ADMIN));
        userDao.insert(User.createWithoutId("user@example.com", "password", "사용자", RoleType.MEMBER));
    }
}
