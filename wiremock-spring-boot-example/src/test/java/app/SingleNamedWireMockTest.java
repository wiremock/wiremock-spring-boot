package app;

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
  @ConfigureWireMock(
      name = "user-client",
      filesUnderClasspath = {"wiremock/user-client"},
      baseUrlProperties = "user-client.url")
})
class SingleNamedWireMockTest {

  @InjectWireMock("user-client")
  private WireMockServer wiremock;

  @Test
  void usesJavaStubbing() {
    this.wiremock.stubFor(
        get("/2")
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
						{ "id": 2, "name": "Amy" }
						""")));

    RestAssured.baseURI = "http://localhost:" + this.wiremock.port();
    final String actual =
        RestAssured.when().get("/2").then().statusCode(200).extract().asPrettyString();
    assertThat(actual)
        .isEqualToIgnoringWhitespace(
            """
				{
				    "id": 2,
				    "name": "Amy"
				}
				""");
  }

  @Test
  void usesStubFiles() {
    RestAssured.baseURI = "http://localhost:" + this.wiremock.port();
    final String actual =
        RestAssured.when().get("/1").then().statusCode(200).extract().asPrettyString();
    assertThat(actual)
        .isEqualToIgnoringWhitespace(
            """
				{
				    "name": "Jenna",
				    "id": 1
				}
				""");
  }
}
