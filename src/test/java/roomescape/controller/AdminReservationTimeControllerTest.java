package roomescape.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.BDDMockito.given;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AdminReservationTimeControllerTest {

    @MockitoBean
    private ClockProvider clockProvider;

    @Autowired
    private DatabaseInitializer databaseInitializer;

    private String adminToken;
    private String userToken;

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

        userToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "user@example.com", "password", "password"))
                .when().post("/login")
                .then().statusCode(200)
                .extract().header("Authorization")
                .replace("Bearer ", "");
    }

    @Test
    void 예약_시간을_추가한다() {
        // when & then
        createTime("10:00")
                .statusCode(201)
                .body("id", notNullValue())
                .header("Location", "/admin/times/1");
    }

    @Test
    void startAt이_null이면_400을_반환한다() {
        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of())
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 예약_시간을_삭제한다() {
        // given
        int timeId = createTime("10:00")
                .statusCode(201)
                .extract().path("id");

        // when & then
        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + adminToken)
                .when().delete("/admin/times/" + timeId)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 존재하지_않는_시간을_삭제하면_404를_반환한다() {
        // when & then
        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + adminToken)
                .when().delete("/admin/times/999")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 예약이_존재하는_시간을_삭제하면_422를_반환한다() {
        // given
        int timeId = createTime("10:00")
                .statusCode(201)
                .extract().path("id");

        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        createReservation(LocalDate.now().plusDays(1).toString(), timeId, themeId);

        // when & then
        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + adminToken)
                .when().delete("/admin/times/" + timeId)
                .then().log().all()
                .statusCode(409);
    }

    private int createTheme(String name, String description, String thumbnail) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("name", name, "description", description, "thumbnail", thumbnail))
                .when().post("/admin/themes")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
    }

    private void createReservation(String date, int timeId, int themeId) {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(Map.of("date", date, "timeId", timeId, "themeId", themeId))
                .when().post("/reservations")
                .then().statusCode(201);
    }

    private ValidatableResponse createTime(String startAt) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("startAt", startAt))
                .when().post("/admin/times")
                .then().log().all();
    }
}
