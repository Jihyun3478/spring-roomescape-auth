package roomescape.shop.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.shop.domain.Shop;

@JdbcTest
@Import(ShopDao.class)
class ShopDaoTest {

    @Autowired
    private ShopDao shopDao;

    @Test
    void 매장_목록을_조회한다() {
        shopDao.insert("달빛방탈출 강남점");
        shopDao.insert("달빛방탈출 홍대점");

        List<Shop> shops = shopDao.select();

        assertThat(shops).hasSize(2);
    }

    @Test
    void 아이디로_매장을_조회한다() {
        Shop saved = shopDao.insert("달빛방탈출 강남점");

        Optional<Shop> found = shopDao.selectById(saved.getId());

        assertAll(
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getName()).isEqualTo("달빛방탈출 강남점")
        );
    }

    @Test
    void 존재하지_않는_아이디로_조회하면_빈_객체를_반환한다() {
        Optional<Shop> found = shopDao.selectById(999L);

        assertThat(found).isEmpty();
    }
}
