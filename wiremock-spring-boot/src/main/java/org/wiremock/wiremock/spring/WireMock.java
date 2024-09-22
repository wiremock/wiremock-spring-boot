package org.wiremock.wiremock.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects WireMock instance previously configured on the class or field level with {@link
 * ConfigureWireMock}.
 *
 * @author Maciej Walkowiak
 * @deprecated to avoid naming collision with {@link
 *     com.github.tomakehurst.wiremock.client.WireMock}, use {@link InjectWireMock} instead.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface WireMock {

  /**
   * The name of WireMock instance to inject.
   *
   * @return the name of WireMock instance to inject.
   */
  String value();
}
