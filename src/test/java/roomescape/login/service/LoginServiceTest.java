package roomescape.login.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.user.dao.UserDao;
import roomescape.user.domain.RoleType;
import roomescape.user.domain.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class LoginServiceTest {

    @Autowired
    private LoginService loginService;

    @Autowired
    private UserDao userDao;

    @Test
    void 로그인에_성공하면_토큰을_반환한다() {
        // given
        User user = saveUser("user@test.com", "password", "브라운", RoleType.MEMBER);

        // when
        String token = loginService.login("user@test.com", "password");

        // then
        assertThat(token).isNotNull();
    }

    @Test
    void 존재하지_않는_이메일로_로그인하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> loginService.login("notexist@test.com", "password"))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 비밀번호가_틀리면_예외가_발생한다() {
        // given
        saveUser("user@test.com", "password", "브라운", RoleType.MEMBER);

        // when & then
        assertThatThrownBy(() -> loginService.login("user@test.com", "wrongpassword"))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 토큰으로_유저를_조회한다() {
        // given
        User user = saveUser("user@test.com", "password", "브라운", RoleType.MEMBER);
        String token = loginService.login("user@test.com", "password");

        // when
        User foundUser = loginService.findUserByToken(token);

        // then
        assertThat(foundUser.getId()).isEqualTo(user.getId());
        assertThat(foundUser.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void 유효하지_않은_토큰으로_유저_조회하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> loginService.findUserByToken("invalidtoken"))
                .isInstanceOf(Exception.class);
    }

    private User saveUser(String email, String password, String name, RoleType roleType) {
        return userDao.insert(User.createWithoutId(email, password, name, roleType));
    }
}
