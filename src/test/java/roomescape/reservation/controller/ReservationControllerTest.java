package roomescape.reservation.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
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
public class ReservationControllerTest {

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
    void 예약을_추가한다() {
        // given
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");

        // when & then
        createReservation(LocalDate.now().plusDays(1).toString(), timeId, themeId)
                .statusCode(201)
                .body("id", notNullValue())
                .header("Location", "/reservations/1");
    }

    @Test
    void 같은_날짜_및_시간이더라도_테마가_다르면_예약이_가능하다() {
        // given
        int timeId = createTime("10:00");
        int themeId1 = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");
        int themeId2 = createTheme("방탈출2", "다함께 탈출해요 방탈출2", "https://asdfsdf.sdfssdafdasf");

        // when & then
        createReservation(LocalDate.now().plusDays(1).toString(), timeId, themeId1).statusCode(201);
        createReservation(LocalDate.now().plusDays(1).toString(), timeId, themeId2).statusCode(201);
    }

    @Test
    void 날짜_형식이_잘못되면_400을_반환한다() {
        // given
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(Map.of("date", "잘못된날짜", "timeId", timeId, "themeId", themeId))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 중복_예약을_하면_409를_반환한다() {
        // given
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        String date = LocalDate.now().plusDays(1).toString();
        createReservation(date, timeId, themeId).statusCode(201);

        // when & then
        createReservation(date, timeId, themeId).statusCode(409);
    }

    @Test
    void 지나간_날짜로_예약하면_422를_반환한다() {
        // given
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");

        // when & then
        createReservation("2026-04-01", timeId, themeId).statusCode(422);
    }

    @Test
    void 내_예약을_조회한다() {
        // given
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");
        createReservation(LocalDate.now().plusDays(1).toString(), timeId, themeId).statusCode(201);

        // when & then
        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userToken)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("name", hasItem("사용자"));
    }

    @Test
    void 예약_날짜_시간을_변경한다() {
        // given
        int timeId1 = createTime("10:00");
        int timeId2 = createTime("11:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        int reservationId = createReservation(LocalDate.now().plusDays(1).toString(), timeId1, themeId)
                .statusCode(201)
                .extract().path("id");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(Map.of("date", LocalDate.now().plusDays(2).toString(), "timeId", timeId2))
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(200)
                .body("date", is(LocalDate.now().plusDays(2).toString()));
    }

    @Test
    void 존재하지_않는_예약을_변경하면_404를_반환한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(Map.of("date", LocalDate.now().plusDays(1).toString(), "timeId", 1))
                .when().patch("/reservations/999")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 지나간_날짜로_변경하면_422를_반환한다() {
        // given
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        int reservationId = createReservation(LocalDate.now().plusDays(1).toString(), timeId, themeId)
                .statusCode(201)
                .extract().path("id");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(Map.of("date", "2026-04-01", "timeId", timeId))
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(422);
    }

    @Test
    void 중복된_날짜_시간으로_변경하면_409를_반환한다() {
        // given
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        String date = LocalDate.now().plusDays(1).toString();
        createReservation(date, timeId, themeId).statusCode(201);

        // 다른 유저로 두 번째 예약
        String token2 = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "admin@example.com", "password", "password"))
                .when().post("/login")
                .then().statusCode(200)
                .extract().header("Authorization")
                .replace("Bearer ", "");

        int reservationId2 = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token2)
                .body(Map.of("date", LocalDate.now().plusDays(2).toString(), "timeId", timeId, "themeId", themeId))
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().path("id");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token2)
                .body(Map.of("date", date, "timeId", timeId))
                .when().patch("/reservations/" + reservationId2)
                .then().log().all()
                .statusCode(409);
    }

    @Test
    void 예약을_삭제한다() {
        // given
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출11", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");
        int reservationId = createReservation(LocalDate.now().plusDays(1).toString(), timeId, themeId)
                .statusCode(201)
                .extract().path("id");

        // when & then
        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userToken)
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 토큰_없이_예약하면_401을_반환한다() {
        // given
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("date", LocalDate.now().plusDays(1).toString(), "timeId", timeId, "themeId", themeId))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(401);
    }

    private int createTime(String startAt) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("startAt", startAt))
                .when().post("/admin/times")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
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

    private ValidatableResponse createReservation(String date, int timeId, int themeId) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(Map.of("date", date, "timeId", timeId, "themeId", themeId))
                .when().post("/reservations")
                .then().log().all();
    }
}
