package usecases;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@EnableWireMock({
  @ConfigureWireMock(filesUnderClasspath = "response-templating", globalTemplating = true)
})
class ResponseTemplatingGlobalTest {

  @Value("${wiremock.server.baseUrl}")
  private String wireMockServerUrl;

  @Test
  void testLocal() {
    RestAssured.baseURI = this.wireMockServerUrl;
    final String actual =
        RestAssured.when().get("/local-templating").then().extract().asPrettyString();
    assertThat(actual)
        .isEqualToIgnoringWhitespace(
            """
        {
            "name": "Resolved: local-templating"
        }
        """);
  }

  @Test
  void testGlobal() {
    RestAssured.baseURI = this.wireMockServerUrl;
    final String actual =
        RestAssured.when().get("/global-templating").then().extract().asPrettyString();
    assertThat(actual)
        .isEqualToIgnoringWhitespace(
            """
        {
            "name": "Resolved: global-templating"
        }
        """);
  }
}
