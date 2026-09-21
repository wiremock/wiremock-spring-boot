package usecases;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * A Spring test that does not opt in to wiremock-spring-boot, but uses WireMock's own {@link
 * WireMockTest} extension. Merely having wiremock-spring-boot on the test classpath must not
 * disturb it.
 *
 * <p>{@link org.wiremock.spring.internal.WireMockTestExecutionListener} is registered for every
 * Spring test via {@code META-INF/spring.factories}, and used to reset the static WireMock DSL
 * client after each test method unconditionally. {@link WireMockTest} configures that client once
 * per test class, so every test method but the first one then talked to {@code localhost:80}
 * instead of the WireMock server (refs #210).
 */
@SpringBootTest
@WireMockTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CoreWireMockTestAnnotationTest {

  @Test
  @Order(1)
  void firstTestMethodCanStubWithStaticDsl(final WireMockRuntimeInfo wireMockRuntimeInfo) {
    assertCanStubWithStaticDsl(wireMockRuntimeInfo);
  }

  @Test
  @Order(2)
  void secondTestMethodCanStubWithStaticDsl(final WireMockRuntimeInfo wireMockRuntimeInfo) {
    assertCanStubWithStaticDsl(wireMockRuntimeInfo);
  }

  private static void assertCanStubWithStaticDsl(final WireMockRuntimeInfo wireMockRuntimeInfo) {
    stubFor(get("/ping").willReturn(ok("pong")));

    RestAssured.when()
        .get(wireMockRuntimeInfo.getHttpBaseUrl() + "/ping")
        .then()
        .statusCode(200)
        .body(Matchers.equalTo("pong"));
  }
}
