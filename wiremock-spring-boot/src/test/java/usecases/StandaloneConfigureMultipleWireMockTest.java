package usecases;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@ConfigureWireMock(name = "user-service1", baseUrlProperties = "user-service1.url")
@ConfigureWireMock(name = "user-service2", baseUrlProperties = "user-service2.url")
class StandaloneConfigureMultipleWireMockTest {

  @InjectWireMock("user-service1")
  private WireMockServer userService1;

  @Value("${user-service1.url}")
  private String userServiceUrl1;

  @InjectWireMock("user-service2")
  private WireMockServer userService2;

  @Value("${user-service2.url}")
  private String userServiceUrl2;

  @Test
  void wireMockIsStarted() {
    assertThat(userService1).isNotNull();
    assertThat(userServiceUrl1).isNotBlank();
    userService1.stubFor(get("/test1").willReturn(aResponse().withStatus(200)));

    assertThat(userService2).isNotNull();
    assertThat(userServiceUrl2).isNotBlank();
    userService2.stubFor(get("/test2").willReturn(aResponse().withStatus(200)));

    RestAssured.when().get(userServiceUrl1 + "/test1").then().statusCode(200);
    RestAssured.when().get(userServiceUrl2 + "/test2").then().statusCode(200);
  }
}
