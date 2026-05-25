package roomescape.shop.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.shop.dao.ShopDao;
import roomescape.shop.dto.response.ShopResponse;

@Service
@Transactional
public class ShopService {

    private final ShopDao shopDao;

    public ShopService(ShopDao shopDao) {
        this.shopDao = shopDao;
    }

    @Transactional(readOnly = true)
    public List<ShopResponse> getShops() {
        return shopDao.select().stream()
                .map(ShopResponse::from)
                .toList();
    }
}
