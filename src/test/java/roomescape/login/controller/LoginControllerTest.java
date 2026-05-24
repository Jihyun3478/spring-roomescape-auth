package roomescape.login.controller;

import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.DatabaseInitializer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class LoginControllerTest {

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @BeforeEach
    void setUp() {
        databaseInitializer.insertDefaultUsers();
    }

    @Test
    void 로그인에_성공한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "user@example.com", "password", "password"))
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .header("Authorization", notNullValue());
    }

    @Test
    void 로그아웃에_성공한다() {
        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "user@example.com", "password", "password"))
                .when().post("/login")
                .then().statusCode(200)
                .extract().header("Authorization")
                .replace("Bearer ", "");

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + token)
                .when().post("/logout")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 존재하지_않는_이메일로_로그인하면_401을_반환한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "notexist@example.com", "password", "password"))
                .when().post("/login")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 비밀번호가_틀리면_401을_반환한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "user@example.com", "password", "wrongpassword"))
                .when().post("/login")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 유효하지_않은_토큰으로_요청하면_401을_반환한다() {
        RestAssured.given().log().all()
                .header("Authorization", "Bearer invalidtoken")
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 사용자가_관리자_API에_접근하면_403을_반환한다() {
        String userToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "user@example.com", "password", "password"))
                .when().post("/login")
                .then().statusCode(200)
                .extract().header("Authorization")
                .replace("Bearer ", "");

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userToken)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(403);
    }
}
