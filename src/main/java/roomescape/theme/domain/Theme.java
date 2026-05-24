package roomescape.theme.domain;

import java.util.Objects;
import roomescape.shop.domain.Shop;

public class Theme {
    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnail;
    private final Shop shop;

    public Theme(Long id, String name, String description, String thumbnail, Shop shop) {
        Objects.requireNonNull(name, "테마 이름은 필수값 입니다.");
        Objects.requireNonNull(description, "테마 설명은 필수값 입니다.");
        Objects.requireNonNull(thumbnail, "테마 썸네일은 필수값 입니다.");
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
        this.shop = shop;
    }

    public static Theme createWithoutId(String name, String description, String thumbnail, Shop shop) {
        return new Theme(null, name, description, thumbnail, shop);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public Shop getShop() {
        return shop;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Theme theme = (Theme) object;
        if (id != null && theme.id != null) {
            return Objects.equals(id, theme.id);
        }
        return Objects.equals(name, theme.name) && Objects.equals(description, theme.description)
                && Objects.equals(thumbnail, theme.thumbnail);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(name, description, thumbnail);
    }
}
