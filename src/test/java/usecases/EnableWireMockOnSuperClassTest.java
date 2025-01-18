package usecases;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@EnableWireMock({
  @ConfigureWireMock(name = "base-service-1", baseUrlProperties = "base-service-1.url"),
  @ConfigureWireMock(name = "base-service-2", baseUrlProperties = "base-service-2.url")
})
class EnableWireMockOnSuperClassTest extends EnableWireMockOnSuperClassTestSuperClass {

  @InjectWireMock("super-service-1")
  private WireMockServer superService1;

  @Value("${super-service-1.url}")
  private String superService1Url;

  @InjectWireMock("super-service-2")
  private WireMockServer superService2;

  @Value("${super-service-2.url}")
  private String superService2Url;

  @InjectWireMock("base-service-1")
  private WireMockServer baseService1;

  @Value("${base-service-1.url}")
  private String baseService1Url;

  @InjectWireMock("base-service-2")
  private WireMockServer baseService2;

  @Value("${base-service-2.url}")
  private String baseService2Url;

  @Test
  void serversConfigured() {
    assertThat(superService1).isNotNull();
    assertThat(superService2).isNotNull();
    assertThat(baseService1).isNotNull();
    assertThat(baseService2).isNotNull();

    assertThat(superService1Url).isNotEqualTo(superService2Url);
    assertThat(superService1Url).isNotEqualTo(baseService1Url);
    assertThat(superService1Url).isNotEqualTo(baseService2Url);

    assertThat(superService2Url).isNotEqualTo(baseService1Url);
    assertThat(superService2Url).isNotEqualTo(baseService2Url);

    assertThat(baseService1Url).isNotEqualTo(baseService2Url);
  }
}
