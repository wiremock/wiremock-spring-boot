package usecases;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@EnableWireMock({@ConfigureWireMock(filesUnderDirectory = "src/test/files-under-directory")})
class FilesUnderDirectoryTests {

  @Value("${wiremock.server.baseUrl}")
  private String wireMockServerUrl;

  @Test
  void test() {
    RestAssured.baseURI = this.wireMockServerUrl;
    final String actual =
        RestAssured.when()
            .get("/files-under-directory")
            .then()
            .statusCode(200)
            .extract()
            .asPrettyString();

    assertThat(actual)
        .isEqualToIgnoringWhitespace(
            """
				{"wiremockmappingsmock":"yes"}
				""");
  }
}
