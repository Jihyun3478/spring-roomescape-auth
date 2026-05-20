package roomescape.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;

import io.restassured.RestAssured;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.DatabaseInitializer;
import roomescape.common.config.ClockProvider;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminUserControllerTest {

    @MockitoBean
    private ClockProvider clockProvider;

    @Autowired
    private DatabaseInitializer databaseInitializer;

    private String adminToken;

    @BeforeEach
    void setUp() {
        databaseInitializer.insertDefaultUsers();

        given(clockProvider.getClock())
                .willReturn(Clock.fixed(
                        Instant.parse("2026-04-28T09:00:00Z"),
                        ZoneOffset.UTC
                ));

        adminToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "admin@example.com", "password", "password"))
                .when().post("/login")
                .then().statusCode(200)
                .extract().header("Authorization")
                .replace("Bearer ", "");
    }

    @Test
    void 회원_목록을_조회한다() {
        // when & then
        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + adminToken)
                .when().get("/admin/users")
                .then().log().all()
                .statusCode(200)
                .body("$", hasSize(1)); // user@example.com (MEMBER) 1명
    }

    @Test
    void 토큰_없이_회원_목록을_조회하면_401을_반환한다() {
        // when & then
        RestAssured.given().log().all()
                .when().get("/admin/users")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 사용자가_회원_목록을_조회하면_403을_반환한다() {
        // given
        String userToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "user@example.com", "password", "password"))
                .when().post("/login")
                .then().statusCode(200)
                .extract().header("Authorization")
                .replace("Bearer ", "");

        // when & then
        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userToken)
                .when().get("/admin/users")
                .then().log().all()
                .statusCode(403);
    }
}
