package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.UserDao;
import roomescape.domain.RoleType;
import roomescape.dto.response.UserResponse;

@Service
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userDao.select(RoleType.MEMBER).stream()
                .map(UserResponse::from)
                .toList();
    }
}
