package usecases;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The reproduction reported in <a
 * href="https://github.com/wiremock/wiremock-spring-boot/issues/210">#210</a>: a Spring test that
 * does not opt in to wiremock-spring-boot, but stubs with the static WireMock DSL through
 * WireMock's own {@link WireMockTest}. Merely having wiremock-spring-boot on the test classpath
 * must not disturb it.
 *
 * <p>It worked in 4.3.0 and broke in 4.4.0, which moved resetting the static DSL client into {@link
 * org.wiremock.spring.internal.WireMockTestExecutionListener}. That listener is registered for
 * every Spring test via {@code META-INF/spring.factories}, and used to reset the client after each
 * test method unconditionally.
 *
 * <p>{@link WireMockTest} configures the static DSL client once per test class, so only a test
 * method that is <em>not</em> the first one fails - the first method's reset is what destroyed the
 * client. Hence the fixed method order, mirroring the order in the issue.
 */
@SpringBootTest
@WireMockTest(httpPort = CoreWireMockTestAnnotationTest.STATIC_HTTP_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CoreWireMockTestAnnotationTest {
  static final int STATIC_HTTP_PORT = 8146;

  private static final String BASE_URL =
      "http://localhost:" + CoreWireMockTestAnnotationTest.STATIC_HTTP_PORT;

  @Test
  @Order(1)
  void status404() {
    stubFor(get("/number").willReturn(notFound()));

    RestAssured.when()
        .get(CoreWireMockTestAnnotationTest.BASE_URL + "/number")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(2)
  void statusOk() {
    stubFor(get("/number").willReturn(okJson("1")));

    RestAssured.when()
        .get(CoreWireMockTestAnnotationTest.BASE_URL + "/number")
        .then()
        .statusCode(200)
        .body(Matchers.equalTo("1"));
  }
}
