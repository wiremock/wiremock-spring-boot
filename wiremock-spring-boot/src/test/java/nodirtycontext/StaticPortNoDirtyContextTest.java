package nodirtycontext;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContextManager;

/**
 * Verifies that {@code staticPortDirtySpringContext = false} actually prevents the Spring test
 * context cache from being evicted between test classes, rather than only checking that the
 * configured port is applied (see #189).
 *
 * <p>Drives two independent {@link TestContextManager} instances for {@link
 * NoDirtyContextTestConfig} directly, simulating two separate test classes with identical
 * configuration, instead of relying on two real JUnit Jupiter test classes and an assumption about
 * which one JUnit happens to run first - Spring's test context cache is shared process-wide, so
 * this is equivalent to running two such classes back to back, without that ordering assumption.
 */
class StaticPortNoDirtyContextTest {

  @Test
  void reusesTheSameContextInsteadOfRebuildingIt() throws Exception {
    ApplicationContext first = this.loadContextAsANewTestClassWould();
    ApplicationContext second = this.loadContextAsANewTestClassWould();

    assertThat(second).isSameAs(first);
    assertThat(ContextCreationCounter.CREATIONS).hasValue(1);
  }

  private ApplicationContext loadContextAsANewTestClassWould() throws Exception {
    TestContextManager testContextManager = new TestContextManager(NoDirtyContextTestConfig.class);
    testContextManager.beforeTestClass();
    Object testInstance = NoDirtyContextTestConfig.class.getDeclaredConstructor().newInstance();
    testContextManager.prepareTestInstance(testInstance);
    ApplicationContext applicationContext =
        testContextManager.getTestContext().getApplicationContext();
    testContextManager.afterTestClass();
    return applicationContext;
  }
}
