package roomescape.reservation.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.DatabaseInitializer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ManagerReservationControllerTest {

    @Autowired
    private DatabaseInitializer databaseInitializer;

    private String managerToken;
    private String manager2Token;
    private String memberToken;
    private String adminToken;
    private long shop1Id;
    private long shop2Id;

    @BeforeEach
    void setUp() {
        databaseInitializer.insertDefaultUsers();

        adminToken = login("admin@example.com");
        memberToken = login("user@example.com");
        managerToken = login("manager1@example.com");
        manager2Token = login("manager2@example.com");

        List<Map<String, Object>> shops = RestAssured.given().log().all()
                .when().get("/shops")
                .then().statusCode(200)
                .extract().jsonPath().getList(".");
        shop1Id = Long.parseLong(shops.get(0).get("id").toString());
        shop2Id = Long.parseLong(shops.get(1).get("id").toString());
    }

    @Test
    void 자기_매장_예약_목록을_조회한다() {
        int timeId = createTime("10:00");
        int themeId = createTheme("강남테마", "설명", "https://thumb.com", shop1Id);
        createAdminReservation("브라운", LocalDate.now().plusDays(1).toString(), timeId, themeId);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + managerToken)
                .when().get("/manager/reservations")
                .then().log().all()
                .statusCode(200)
                .body("$", hasSize(1));
    }

    @Test
    void 미인증_요청은_401을_반환한다() {
        RestAssured.given().log().all()
                .when().get("/manager/reservations")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void MEMBER가_매니저_API에_접근하면_403을_반환한다() {
        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + memberToken)
                .when().get("/manager/reservations")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    void ADMIN이_매니저_API에_접근하면_403을_반환한다() {
        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + adminToken)
                .when().get("/manager/reservations")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    void 다른_매장_예약을_삭제하면_404를_반환한다() {
        int timeId = createTime("10:00");
        int themeId = createTheme("홍대테마", "설명", "https://thumb.com", shop2Id);
        int reservationId = createAdminReservation("브라운",
                LocalDate.now().plusDays(1).toString(), timeId, themeId);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + managerToken)
                .when().delete("/manager/reservations/" + reservationId)
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 자기_매장_예약을_변경한다() {
        int timeId1 = createTime("10:00");
        int timeId2 = createTime("11:00");
        int themeId = createTheme("강남테마", "설명", "https://thumb.com", shop1Id);
        int reservationId = createAdminReservation("브라운",
                LocalDate.now().plusDays(1).toString(), timeId1, themeId);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + managerToken)
                .contentType(ContentType.JSON)
                .body(Map.of("date", LocalDate.now().plusDays(2).toString(), "timeId", timeId2))
                .when().patch("/manager/reservations/" + reservationId)
                .then().log().all()
                .statusCode(200)
                .body("date", is(LocalDate.now().plusDays(2).toString()));
    }

    @Test
    void 자기_매장_예약을_삭제한다() {
        int timeId = createTime("10:00");
        int themeId = createTheme("강남테마", "설명", "https://thumb.com", shop1Id);
        int reservationId = createAdminReservation("브라운",
                LocalDate.now().plusDays(1).toString(), timeId, themeId);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + managerToken)
                .when().delete("/manager/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);
    }

    private String login(String email) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", "password"))
                .when().post("/login")
                .then().statusCode(200)
                .extract().header("Authorization")
                .replace("Bearer ", "");
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

    private int createTheme(String name, String description, String thumbnail, long shopId) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("name", name, "description", description,
                        "thumbnail", thumbnail, "shopId", shopId))
                .when().post("/admin/themes")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
    }

    private int createAdminReservation(String name, String date, int timeId, int themeId) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("name", name, "date", date, "timeId", timeId, "themeId", themeId))
                .when().post("/admin/reservations")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
    }
}
