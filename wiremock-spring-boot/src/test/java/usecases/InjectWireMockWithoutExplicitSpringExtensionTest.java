package usecases;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.InjectWireMock;

/**
 * Regression test for <a
 * href="https://github.com/wiremock/wiremock-spring-boot/issues/212">#212</a>:
 * {@code @InjectWireMock} must work even when the test class registers neither
 * {@code @SpringBootTest}, {@code @SpringJUnitConfig} nor
 * {@code @ExtendWith(SpringExtension.class)} itself, relying only on what
 * {@code @ConfigureWireMock} brings in.
 */
@ConfigureWireMock(name = "my-service")
class InjectWireMockWithoutExplicitSpringExtensionTest {

  @InjectWireMock("my-service")
  private WireMockServer wireMock;

  @Test
  void injectsFieldAndStartsServer() {
    assertThat(wireMock).isNotNull();

    wireMock.stubFor(get("/ping").willReturn(aResponse().withStatus(200)));

    RestAssured.when().get(wireMock.baseUrl() + "/ping").then().statusCode(200);
  }
}
