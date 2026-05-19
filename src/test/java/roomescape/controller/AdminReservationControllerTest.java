package roomescape.controller;

import static org.hamcrest.Matchers.hasItem;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.common.config.ClockProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AdminReservationControllerTest {

    @MockitoBean
    private ClockProvider clockProvider;

    @BeforeEach
    void setUp() {
        given(clockProvider.getClock())
                .willReturn(Clock.fixed(
                        Instant.parse("2026-04-28T09:00:00Z"),
                        ZoneOffset.UTC
                ));
    }

    @Test
    void 예약을_추가한다() {
        // given
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");

        // when & then
        createReservation("브라운", LocalDate.now().plusDays(1).toString(), timeId, themeId)
                .statusCode(201)
                .body("id", notNullValue())
                .header("Location", "/admin/reservations/1");
    }

    @Test
    void 예약을_조회한다() {
        // given
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");
        createReservation("브라운", LocalDate.now().plusDays(1).toString(), timeId, themeId).statusCode(201);

        // when & then
        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("name", hasItem("브라운"));
    }

    @Test
    void 예약을_삭제한다() {
        // given
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출11", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");
        int reservationId = createReservation("브라운", LocalDate.now().plusDays(1).toString(), timeId, themeId)
                .statusCode(201)
                .extract().path("id");

        // when & then
        RestAssured.given().log().all()
                .when().delete("/admin/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);
    }

    private int createTime(String startAt) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", startAt))
                .when().post("/admin/times")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
    }

    private int createTheme(String name, String description, String thumbnail) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "description", description, "thumbnail", thumbnail))
                .when().post("/admin/themes")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
    }

    private ValidatableResponse createReservation(String name, String date, int timeId, int themeId) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "date", date, "timeId", timeId, "themeId", themeId))
                .when().post("/admin/reservations")
                .then().log().all();
    }
}

