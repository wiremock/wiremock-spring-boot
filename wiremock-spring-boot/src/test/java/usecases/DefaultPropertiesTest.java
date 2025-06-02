package usecases;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@EnableWireMock
class DefaultPropertiesTest {

  @Value("${wiremock.server.baseUrl}")
  private String wiremockUrl;

  @Value("${wiremock.server.port}")
  private String wiremockPort;

  @InjectWireMock WireMockServer wireMockServer;

  @BeforeEach
  public void before() {}

  @Test
  void testCanInvoke() {
    WireMock.stubFor(get("/the_default_prop_mock").willReturn(aResponse().withStatus(202)));

    RestAssured.baseURI = this.wiremockUrl;
    RestAssured.when().get("/the_default_prop_mock").then().statusCode(202);
  }

  @Test
  void testUrlNotNull() {
    assertThat(this.wiremockUrl).isNotNull();
  }

  @Test
  void testPortNotNull() {
    assertThat(this.wiremockPort).isNotNull();
  }
}
