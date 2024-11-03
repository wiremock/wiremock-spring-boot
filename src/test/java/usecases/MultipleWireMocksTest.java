package usecases;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@EnableWireMock({
  @ConfigureWireMock(name = "user-client", baseUrlProperties = "user-client.url"),
  @ConfigureWireMock(name = "todo-service", baseUrlProperties = "todo-client.url")
})
class MultipleWireMocksTest {

  @InjectWireMock("todo-service")
  private WireMockServer todoService;

  @InjectWireMock("user-client")
  private WireMockServer userService;

  @Test
  void returnsTodos() {
    this.todoService.stubFor(
        get("/")
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
						[
						    { "id": 1, "userId": 1, "title": "my todo" },
						    { "id": 2, "userId": 2, "title": "my todo2" }
						]
						""")));

    this.userService.stubFor(
        get("/1")
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
						{ "id": 1, "name": "Amy" }
						""")));

    this.userService.stubFor(
        get("/2")
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
						{ "id": 2, "name": "John" }
						""")));

    assertThat(
            RestAssured.when()
                .get("http://localhost:" + this.todoService.port() + "/")
                .then()
                .statusCode(200)
                .extract()
                .asPrettyString())
        .isEqualToIgnoringWhitespace(
            """
						[
						    {
						        "id": 1,
						        "userId": 1,
						        "title": "my todo"
						    },
						    {
						        "id": 2,
						        "userId": 2,
						        "title": "my todo2"
						    }
						]
						""");

    assertThat(
            RestAssured.when()
                .get("http://localhost:" + this.userService.port() + "/1")
                .then()
                .statusCode(200)
                .extract()
                .asPrettyString())
        .isEqualToIgnoringWhitespace(
            """
						{
						    "id": 1,
						    "name": "Amy"
						}
						""");

    assertThat(
            RestAssured.when()
                .get("http://localhost:" + this.userService.port() + "/2")
                .then()
                .statusCode(200)
                .extract()
                .asPrettyString())
        .isEqualToIgnoringWhitespace(
            """
						{
						    "id": 2,
						    "name": "John"
						}
						""");
  }
}
