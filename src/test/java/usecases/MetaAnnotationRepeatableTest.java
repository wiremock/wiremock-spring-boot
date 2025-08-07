package usecases;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@EnableWireMock({@ConfigureWireMock(name = "direct-wiremock")})
@MetaAnnotationRepeatableTest.MetaAnnotation
public class MetaAnnotationRepeatableTest {

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @SpringBootTest
  @EnableWireMock(@ConfigureWireMock(name = "meta-wiremock"))
  @interface MetaAnnotation {}

  @InjectWireMock("direct-wiremock")
  private WireMockServer directWireMock;

  @InjectWireMock("meta-wiremock")
  private WireMockServer metaWireMockServer;

  @Test
  void metaAnnotationAndDirectAnnotationBothGetRegistered() {
    assertThat(directWireMock).isNotNull();
    assertThat(metaWireMockServer).isNotNull();
  }
}
