package org.wiremock.spring.internal;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class WireMockTestExecutionListener extends AbstractTestExecutionListener {
  private static boolean isDirty = false;

  public static void markContextAsDirty() {
    isDirty = true;
  }

  @Override
  @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
  public void afterTestClass(TestContext testContext) {
    if (isDirty) {
      isDirty = false;
      testContext.markApplicationContextDirty(DirtiesContext.HierarchyMode.EXHAUSTIVE);
    }
  }
}
