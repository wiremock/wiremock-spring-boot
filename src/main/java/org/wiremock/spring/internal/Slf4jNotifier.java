package org.wiremock.spring.internal;

import com.github.tomakehurst.wiremock.common.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ported from:
// https://github.com/spring-cloud/spring-cloud-contract/commit/44c634d0e9e82515d2fba66343530eb7d2ba8223
class Slf4jNotifier implements Notifier {

  private final Logger log;

  Slf4jNotifier(final String name) {
    this.log = LoggerFactory.getLogger("WireMock " + name);
  }

  @Override
  public void info(final String message) {
    this.log.info(message);
  }

  @Override
  public void error(final String message) {
    this.log.error(message);
  }

  @Override
  public void error(final String message, final Throwable t) {
    this.log.error(message, t);
  }
}
