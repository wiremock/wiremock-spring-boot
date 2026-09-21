package corewiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpClientErrorException;

/**
 * The reproduction reported in <a
 * href="https://github.com/wiremock/wiremock-spring-boot/issues/210">#210</a>: a Spring test that
 * stubs with the static WireMock DSL through WireMock's own {@link WireMockTest}, against a service
 * bean pointed at a statically configured port.
 *
 * <p>It worked in 4.3.0 and broke in 4.4.0. The test methods are ordered because only a test method
 * that is <em>not</em> the first one fails: {@link WireMockTest} configures the static DSL client
 * once per test class, and {@code WireMockTestExecutionListener.afterTestMethod} used to reset that
 * client after every test method of every Spring test, whether or not it opted in to
 * wiremock-spring-boot. The second {@code stubFor} then addressed {@code localhost:80}.
 *
 * @see CoreWireMockTestAnnotationTest
 */
@SpringBootTest
@WireMockTest(httpPort = GetNumberAdapterTest.STATIC_HTTP_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GetNumberAdapterTest {
  static final int STATIC_HTTP_PORT = 8146;

  @Autowired private GetNumberAdapter getNumberAdapter;

  @Test
  @Order(1)
  void status404() {
    stubFor(get("/number").willReturn(notFound()));

    assertThatThrownBy(() -> this.getNumberAdapter.getNumber())
        .isInstanceOf(HttpClientErrorException.class);
  }

  @Test
  @Order(2)
  void statusOk() {
    stubFor(get("/number").willReturn(okJson("1")));

    assertThat(this.getNumberAdapter.getNumber()).isEqualTo(1);
  }
}
