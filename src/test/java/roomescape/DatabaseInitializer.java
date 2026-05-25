package roomescape;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import roomescape.manager.dao.ManagerDao;
import roomescape.shop.dao.ShopDao;
import roomescape.shop.domain.Shop;
import roomescape.user.dao.UserDao;
import roomescape.user.domain.RoleType;
import roomescape.user.domain.User;

@Component
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;
    private final UserDao userDao;
    private final ShopDao shopDao;
    private final ManagerDao managerDao;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate, UserDao userDao,
                               ShopDao shopDao, ManagerDao managerDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDao = userDao;
        this.shopDao = shopDao;
        this.managerDao = managerDao;
    }

    public void clear() {
        jdbcTemplate.update("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.update("TRUNCATE TABLE manager");
        jdbcTemplate.update("TRUNCATE TABLE users");
        jdbcTemplate.update("TRUNCATE TABLE reservation");
        jdbcTemplate.update("TRUNCATE TABLE reservation_time");
        jdbcTemplate.update("TRUNCATE TABLE theme");
        jdbcTemplate.update("TRUNCATE TABLE shop");
        jdbcTemplate.update("SET REFERENTIAL_INTEGRITY TRUE");
    }

    public void insertDefaultUsers() {
        userDao.insert(User.createWithoutId("admin@example.com", "password", "관리자", RoleType.ADMIN));
        userDao.insert(User.createWithoutId("user@example.com", "password", "사용자", RoleType.MEMBER));
        User manager1 = userDao.insert(User.createWithoutId("manager1@example.com", "password", "매니저1", RoleType.MANAGER));
        User manager2 = userDao.insert(User.createWithoutId("manager2@example.com", "password", "매니저2", RoleType.MANAGER));

        Shop shop1 = shopDao.insert("달빛방탈출 강남점");
        Shop shop2 = shopDao.insert("달빛방탈출 홍대점");

        managerDao.insert(manager1.getId(), shop1.getId());
        managerDao.insert(manager2.getId(), shop2.getId());
    }
}
