package app;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@EnableWireMock({
  @ConfigureWireMock(
      name = "fs-client",
      filesUnderClasspath = "does_not_exist",
      filesUnderDirectory = "src/test/wiremock-mappings")
})
class FilesUnderDirectoryTests {

  @Value("${wiremock.server.baseUrl}")
  private String wireMockServerUrl;

  @Test
  void test() {
    RestAssured.baseURI = this.wireMockServerUrl;
    RestAssured.when().get("/wiremockmappingsmock").then().statusCode(200);
  }
}
