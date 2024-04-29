package app;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableWireMock({
        @ConfigureWireMock(name = "user-client", property = "user-client.url", stubLocation = "src/test/wiremock-mappings/user-client", stubLocationOnClasspath = false)
})
class StubsInDirectoryTests {

    @Autowired
    private UserClient userClient;

    @InjectWireMock("user-client")
    private WireMockServer wiremock;

    @Test
    void usesStubFiles() {
        User user = userClient.findOne(1L);
        assertThat(user).isNotNull();
    }

}
