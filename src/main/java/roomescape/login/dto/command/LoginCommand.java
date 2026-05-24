package roomescape.login.dto.command;

public record LoginCommand(
        String email,
        String password
) {
}
