package roomescape.shop.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.shop.domain.Shop;

@Repository
public class ShopDao {
    private static final RowMapper<Shop> ROW_MAPPER = (resultSet, rowNum) -> {
        return new Shop(
                resultSet.getLong("id"),
                resultSet.getString("name")
        );
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ShopDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("shop")
                .usingGeneratedKeyColumns("id");
    }

    public Shop insert(String name) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", name);

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);
        return new Shop(generatedId.longValue(), name);
    }

    public List<Shop> select() {
        return jdbcTemplate.query(baseSelectSql(), ROW_MAPPER);
    }

    public Optional<Shop> selectById(long shopId) {
        try {
            String sql = baseSelectSql() + " WHERE id = ?";
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, shopId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private String baseSelectSql() {
        return """
                SELECT id,
                       name
                FROM shop
                """;
    }
}
