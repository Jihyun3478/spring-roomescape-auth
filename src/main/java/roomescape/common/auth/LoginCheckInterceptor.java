package roomescape.common.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.user.domain.RoleType;
import roomescape.user.domain.User;
import roomescape.login.service.LoginService;

public class LoginCheckInterceptor implements HandlerInterceptor {

    private final LoginService loginService;

    public LoginCheckInterceptor(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");
        if (Objects.isNull(authorization)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        String token = authorization.split(" ")[1];

        try {
            User user = loginService.findUserByToken(token);
            request.setAttribute("loginUser", user);

            if (request.getRequestURI().startsWith("/admin") && user.getRoleType() != RoleType.ADMIN) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
        } catch (Exception exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        return true;
    }
}
