package nodirtycontext;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/**
 * Counts how many times {@link NoDirtyContextApp}'s context has actually been created in this JVM.
 * Component-scanned into every context built from {@link NoDirtyContextApp}, so its
 * {@code @PostConstruct} runs once per real context creation, not once per test class - a context
 * reused from Spring's test context cache does not re-create this bean.
 */
@Component
class ContextCreationCounter {
  static final AtomicInteger CREATIONS = new AtomicInteger();

  @PostConstruct
  void onContextCreated() {
    CREATIONS.incrementAndGet();
  }
}
