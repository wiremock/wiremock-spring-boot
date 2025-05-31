package usecases;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@EnableWireMock({@ConfigureWireMock(name = DefaultDirectoryTest.DEFAULT_DIRECTORY_WM_NAME)})
class DefaultDirectoryTest {
  public static final String DEFAULT_DIRECTORY_WM_NAME = "test-mappings";

  @InjectWireMock(DefaultDirectoryTest.DEFAULT_DIRECTORY_WM_NAME)
  private WireMockServer wiremock;

  @Test
  void usesStubFiles() {
    RestAssured.baseURI = "http://localhost:" + this.wiremock.port();
    final String actual =
        RestAssured.when().get("/1").then().statusCode(200).extract().asPrettyString();
    assertThat(actual)
        .isEqualToIgnoringWhitespace(
            """
				{
				    "name": "Stuff",
				    "id": 1
				}
				""");
  }
}
