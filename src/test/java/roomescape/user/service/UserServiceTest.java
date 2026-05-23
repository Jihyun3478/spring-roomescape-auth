package roomescape.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.user.dao.UserDao;
import roomescape.user.domain.RoleType;
import roomescape.user.domain.User;
import roomescape.user.dto.response.UserResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;

    @Test
    void MEMBER_목록을_조회한다() {
        // given
        userDao.insert(User.createWithoutId("member1@test.com", "password", "브라운", RoleType.MEMBER));
        userDao.insert(User.createWithoutId("member2@test.com", "password", "로지", RoleType.MEMBER));
        userDao.insert(User.createWithoutId("admin@test.com", "password", "관리자", RoleType.ADMIN));

        // when
        List<UserResponse> responses = userService.getAllUsers();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(UserResponse::name)
                .containsExactlyInAnyOrder("브라운", "로지");
    }

    @Test
    void MEMBER가_없으면_빈_목록을_반환한다() {
        // given
        userDao.insert(User.createWithoutId("admin@test.com", "password", "관리자", RoleType.ADMIN));

        // when
        List<UserResponse> responses = userService.getAllUsers();

        // then
        assertThat(responses).isEmpty();
    }
}
