package usecases;

import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@EnableWireMock({
  @ConfigureWireMock(name = "super-service-1", baseUrlProperties = "super-service-1.url"),
  @ConfigureWireMock(name = "super-service-2", baseUrlProperties = "super-service-2.url")
})
public class EnableWireMockOnSuperClassTestSuperClass {}
