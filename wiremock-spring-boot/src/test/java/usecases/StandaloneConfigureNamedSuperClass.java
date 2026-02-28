package usecases;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@ConfigureWireMock(name = "user-service1", baseUrlProperties = "user-service1.url")
@ConfigureWireMock(name = "user-service2", baseUrlProperties = "user-service2.url")
class StandaloneConfigureNamedSuperClass {
  @InjectWireMock("user-service1")
  WireMockServer userService1;

  @Value("${user-service1.url}")
  String userServiceUrl1;

  @InjectWireMock("user-service2")
  WireMockServer userService2;

  @Value("${user-service2.url}")
  String userServiceUrl2;
}
