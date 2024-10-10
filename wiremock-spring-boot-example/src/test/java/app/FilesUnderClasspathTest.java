package app;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@EnableWireMock({@ConfigureWireMock(filesUnderClasspath = "custom-location")})
class FilesUnderClasspathTest {

  @Value("${wiremock.server.baseUrl}")
  private String wireMockServerUrl;

  @Test
  void test() {
    RestAssured.baseURI = this.wireMockServerUrl;
    final String actual =
        RestAssured.when()
            .get("/classpathmappings")
            .then()
            .statusCode(200)
            .extract()
            .asPrettyString();
    assertThat(actual)
        .isEqualToIgnoringWhitespace(
            """
				[
				    {
				        "id": 1,
				        "title": "custom location todo 1",
				        "userId": 1
				    },
				    {
				        "id": 2,
				        "title": "custom location todo 2",
				        "userId": 1
				    }
				]
				""");
  }
}
