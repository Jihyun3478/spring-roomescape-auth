package roomescape.common.auth;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.UserErrorCode;
import roomescape.domain.User;

public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(LoginUser.class);
        boolean isMemberType = User.class.isAssignableFrom(parameter.getParameterType());

        return hasAnnotation && isMemberType;
    }


    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        User user = (User) request.getAttribute("loginUser");

        if (Objects.isNull(user)) {
            throw new RoomEscapeException(UserErrorCode.NOT_FOUND);
        }
        return user;
    }
}
