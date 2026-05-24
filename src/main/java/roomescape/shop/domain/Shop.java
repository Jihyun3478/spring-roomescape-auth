package roomescape.shop.domain;

public class Shop {
    private final Long id;
    private final String name;

    public Shop(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Shop createWithoutId(String name) {
        return new Shop(null, name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
