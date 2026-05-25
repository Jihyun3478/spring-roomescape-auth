package roomescape.shop.dto.response;

import roomescape.shop.domain.Shop;

public record ShopResponse(
        Long id,
        String name
) {
    public static ShopResponse from(Shop shop) {
        return new ShopResponse(shop.getId(), shop.getName());
    }
}
