package usecases;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.InjectWireMock;

@ConfigureWireMock(name = "user-service3", baseUrlProperties = "user-service3.url")
class StandaloneConfigureNamedSuperAndDirectWireMockTest
    extends StandaloneConfigureNamedSuperClass {

  @InjectWireMock("user-service3")
  private WireMockServer userService3;

  @Value("${user-service3.url}")
  private String userServiceUrl3;

  @Test
  void wireMockIsStarted() {
    assertThat(userService1).isNotNull();
    assertThat(userServiceUrl1).isNotBlank();
    userService1.stubFor(get("/test1").willReturn(aResponse().withStatus(200)));

    assertThat(userService2).isNotNull();
    assertThat(userServiceUrl2).isNotBlank();
    userService2.stubFor(get("/test2").willReturn(aResponse().withStatus(200)));

    assertThat(userService3).isNotNull();
    assertThat(userServiceUrl3).isNotBlank();
    userService3.stubFor(get("/test3").willReturn(aResponse().withStatus(200)));

    RestAssured.when().get(userServiceUrl1 + "/test1").then().statusCode(200);
    RestAssured.when().get(userServiceUrl2 + "/test2").then().statusCode(200);
    RestAssured.when().get(userServiceUrl3 + "/test3").then().statusCode(200);
  }
}
