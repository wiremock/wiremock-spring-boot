package app;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@EnableWireMock({@ConfigureWireMock(useHttps = true)})
class HttpsTest {

  @InjectWireMock private WireMockServer wiremock;

  @Value("${wiremock.server.port}")
  private int wiremockPort;

  @Value("${wiremock.server.baseUrl}")
  private String wiremockUrl;

  @BeforeEach
  public void before() {
    RestAssured.useRelaxedHTTPSValidation();
  }

  @Test
  void testProperties() {
    assertThat(this.wiremockPort).isNotNull();
    assertThat(this.wiremockUrl).startsWith("https://").contains(String.valueOf(this.wiremockPort));
  }

  @Test
  void testInjectedClient() {
    this.wiremock.stubFor(get("/injected-client").willReturn(aResponse().withStatus(202)));

    RestAssured.when().get(this.wiremockUrl + "/injected-client").then().statusCode(202);

    assertThat(this.wiremock.findAll(anyRequestedFor(anyUrl()))).hasSize(1);
  }

  @Test
  void testDefaultClient() {
    WireMock.stubFor(WireMock.get("/with-default-client").willReturn(aResponse().withStatus(202)));

    RestAssured.when().get(this.wiremockUrl + "/with-default-client").then().statusCode(202);

    assertThat(WireMock.findAll(anyRequestedFor(anyUrl()))).hasSize(1);
  }
}
