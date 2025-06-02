package usecases.cucumber;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = RANDOM_PORT)
@EnableWireMock(
    @ConfigureWireMock(name = CucumberConstants.WIREMOCK_SERVER_NAME, registerSpringBean = true))
public class CucumberSpringConfiguration {}
