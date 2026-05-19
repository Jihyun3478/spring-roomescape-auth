package roomescape.service;

import java.util.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.UserErrorCode;
import roomescape.dao.UserDao;
import roomescape.domain.User;

@Service
@Transactional
public class LoginService {

    private final UserDao userDao;

    public LoginService(UserDao userDao) {
        this.userDao = userDao;
    }

    public String login(String email, String password) {
        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new RoomEscapeException(UserErrorCode.NOT_FOUND));

        if (!user.getPassword().equals(password)) {
            throw new RoomEscapeException(UserErrorCode.INVALID_PASSWORD);
        }
        return createToken(user);
    }

    public User findUserByToken(String token) {
        long userId = extractUserId(token);
        return userDao.findById(userId)
                .orElseThrow(() -> new RoomEscapeException(UserErrorCode.NOT_FOUND));
    }

    private String createToken(User user) {
        return Base64.getEncoder().encodeToString(
                ("userId=" + user.getId() + "&role=" + user.getRoleType().name()).getBytes()
        );
    }

    private long extractUserId(String token) {
        String decoded = new String(Base64.getDecoder().decode(token));
        return Long.parseLong(decoded.split("&")[0].split("=")[1]);
    }
}
