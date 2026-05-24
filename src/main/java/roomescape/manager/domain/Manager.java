package roomescape.manager.domain;

import roomescape.shop.domain.Shop;
import roomescape.user.domain.User;

public class Manager {
    private final Long id;
    private final User user;
    private final Shop shop;

    public Manager(Long id, User user, Shop shop) {
        this.id = id;
        this.user = user;
        this.shop = shop;
    }

    public static Manager createWithoutId(User user, Shop shop) {
        return new Manager(null, user, shop);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Shop getShop() {
        return shop;
    }
}
