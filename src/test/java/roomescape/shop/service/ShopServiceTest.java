package roomescape.shop.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.DatabaseInitializer;
import roomescape.shop.dao.ShopDao;
import roomescape.shop.dto.response.ShopResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ShopServiceTest {

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @Autowired
    private ShopService shopService;

    @Autowired
    private ShopDao shopDao;

    @BeforeEach
    void setUp() {
        databaseInitializer.clear();
    }

    @Test
    void 매장_목록을_조회한다() {
        shopDao.insert("달빛방탈출 강남점");
        shopDao.insert("달빛방탈출 홍대점");

        List<ShopResponse> responses = shopService.getShops();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("name")
                .containsExactly("달빛방탈출 강남점", "달빛방탈출 홍대점");
    }
}
