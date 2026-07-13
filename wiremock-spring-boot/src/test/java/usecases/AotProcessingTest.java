package usecases;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.aot.generate.InMemoryGeneratedFiles;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.aot.TestContextAotGenerator;
import org.wiremock.spring.EnableWireMock;

class AotProcessingTest {

  @SpringBootTest
  @EnableWireMock
  static class AotProcessedTest {}

  @Test
  void wireMockIsStoppedWhenAotProcessingClosesContext() throws InterruptedException {
    final Set<String> poolsBefore =
        this.nonDaemonJettyThreads().stream()
            .map(this::jettyThreadPoolName)
            .collect(Collectors.toSet());

    new TestContextAotGenerator(new InMemoryGeneratedFiles(), new RuntimeHints(), true)
        .processAheadOfTime(Stream.of(AotProcessedTest.class));

    final Set<Thread> leaked = this.leakedJettyThreads(poolsBefore);
    final long deadline = System.currentTimeMillis() + 10_000;
    while (!leaked.isEmpty() && System.currentTimeMillis() < deadline) {
      Thread.sleep(100);
      leaked.removeIf(thread -> !thread.isAlive());
    }
    assertThat(leaked)
        .as("non-daemon Jetty threads still running after AOT processing closed the context")
        .isEmpty();
  }

  private Set<Thread> leakedJettyThreads(final Set<String> poolsBefore) {
    return this.nonDaemonJettyThreads().stream()
        .filter(thread -> !poolsBefore.contains(this.jettyThreadPoolName(thread)))
        .collect(Collectors.toSet());
  }

  private Set<Thread> nonDaemonJettyThreads() {
    return Thread.getAllStackTraces().keySet().stream()
        .filter(thread -> !thread.isDaemon())
        .filter(thread -> thread.getName().startsWith("qtp"))
        .collect(Collectors.toSet());
  }

  private String jettyThreadPoolName(final Thread thread) {
    return thread.getName().replaceFirst("-\\d+$", "");
  }
}
