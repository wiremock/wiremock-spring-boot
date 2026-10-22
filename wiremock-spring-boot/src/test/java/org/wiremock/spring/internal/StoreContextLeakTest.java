package org.wiremock.spring.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.ref.WeakReference;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.wiremock.spring.ConfigureWireMock;

class StoreContextLeakTest {

  @ConfigureWireMock(name = "leak-test")
  static class Configured {}

  @Test
  void closedContextCanBeGarbageCollected() throws InterruptedException {
    WeakReference<AnnotationConfigApplicationContext> contextRef = startAndCloseContext();

    for (int i = 0; i < 50 && contextRef.get() != null; i++) {
      System.gc();
      Thread.sleep(50);
    }

    assertThat(contextRef.get()).isNull();
  }

  private static WeakReference<AnnotationConfigApplicationContext> startAndCloseContext() {
    ConfigureWireMock options = Configured.class.getAnnotation(ConfigureWireMock.class);
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.refresh();
    new WireMockServerCreator(options.name()).createWireMockServer(context, options);
    context.close();
    return new WeakReference<>(context);
  }
}
