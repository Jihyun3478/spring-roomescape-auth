package roomescape.common.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.common.auth.LoginCheckInterceptor;
import roomescape.common.auth.LoginUserArgumentResolver;
import roomescape.login.service.LoginService;

@Configuration
public class AuthenticationPrincipalConfig implements WebMvcConfigurer {

    private final LoginService loginService;

    public AuthenticationPrincipalConfig(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor(loginService))
                .addPathPatterns("/reservations/**", "/admin/**", "/manager/**", "/logout")
                .excludePathPatterns(
                        "/login",
                        "/themes/popular"
                );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginUserArgumentResolver());
    }
}
