package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.RoleType;
import roomescape.domain.User;

@JdbcTest
@Import(UserDao.class)
class UserDaoTest {

    @Autowired
    private UserDao userDao;

    @Test
    void 유저를_저장한다() {
        // given
        User user = User.createWithoutId("user@test.com", "password", "브라운", RoleType.MEMBER);

        // when
        User saved = userDao.insert(user);

        // then
        assertAll(
                () -> assertThat(saved.getId()).isNotNull(),
                () -> assertThat(saved.getEmail()).isEqualTo("user@test.com"),
                () -> assertThat(saved.getName()).isEqualTo("브라운"),
                () -> assertThat(saved.getRoleType()).isEqualTo(RoleType.MEMBER)
        );
    }

    @Test
    void 이메일로_유저를_조회한다() {
        // given
        userDao.insert(User.createWithoutId("user@test.com", "password", "브라운", RoleType.MEMBER));

        // when
        Optional<User> found = userDao.findByEmail("user@test.com");

        // then
        assertAll(
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getEmail()).isEqualTo("user@test.com"),
                () -> assertThat(found.get().getName()).isEqualTo("브라운")
        );
    }

    @Test
    void 존재하지_않는_이메일로_조회하면_빈_객체를_반환한다() {
        // when
        Optional<User> found = userDao.findByEmail("notexist@test.com");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 아이디로_유저를_조회한다() {
        // given
        User saved = userDao.insert(User.createWithoutId("user@test.com", "password", "브라운", RoleType.MEMBER));

        // when
        Optional<User> found = userDao.findById(saved.getId());

        // then
        assertAll(
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getId()).isEqualTo(saved.getId()),
                () -> assertThat(found.get().getEmail()).isEqualTo("user@test.com")
        );
    }

    @Test
    void 존재하지_않는_아이디로_조회하면_빈_객체를_반환한다() {
        // when
        Optional<User> found = userDao.findById(999L);

        // then
        assertThat(found).isEmpty();
    }
}
