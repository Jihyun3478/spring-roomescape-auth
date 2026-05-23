package roomescape.user.domain;

public class User {
    private final Long id;
    private final String email;
    private final String password;
    private final String name;
    private final RoleType roleType;

    public User(Long id, String email, String password, String name, RoleType roleType) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.roleType = roleType;
    }

    public static User createWithoutId(String email, String password, String name, RoleType roleType) {
        return new User(null, email, password, name, roleType);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public RoleType getRoleType() {
        return roleType;
    }
}
