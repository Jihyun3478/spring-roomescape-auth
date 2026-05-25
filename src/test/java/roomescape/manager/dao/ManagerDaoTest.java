package roomescape.manager.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.manager.domain.Manager;
import roomescape.shop.dao.ShopDao;
import roomescape.shop.domain.Shop;
import roomescape.user.dao.UserDao;
import roomescape.user.domain.RoleType;
import roomescape.user.domain.User;

@JdbcTest
@Import({ManagerDao.class, ShopDao.class, UserDao.class})
class ManagerDaoTest {

    @Autowired
    private ManagerDao managerDao;

    @Autowired
    private ShopDao shopDao;

    @Autowired
    private UserDao userDao;

    @Test
    void 유저_아이디로_매니저를_조회한다() {
        User user = userDao.insert(User.createWithoutId("manager@test.com", "password", "매니저", RoleType.MANAGER));
        Shop shop = shopDao.insert("달빛방탈출 강남점");
        managerDao.insert(user.getId(), shop.getId());

        Optional<Manager> found = managerDao.selectByUserId(user.getId());

        assertAll(
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getUser().getId()).isEqualTo(user.getId()),
                () -> assertThat(found.get().getShop().getId()).isEqualTo(shop.getId()),
                () -> assertThat(found.get().getShop().getName()).isEqualTo("달빛방탈출 강남점")
        );
    }

    @Test
    void 존재하지_않는_유저_아이디로_조회하면_빈_객체를_반환한다() {
        Optional<Manager> found = managerDao.selectByUserId(999L);

        assertThat(found).isEmpty();
    }
}
