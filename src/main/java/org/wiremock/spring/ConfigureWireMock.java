package org.wiremock.spring;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Extension;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Configures WireMock instance.
 *
 * @author Maciej Walkowiak
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigureWireMock {

  /**
   * Port on which WireMock server is going to listen. {@code 0} means WireMock will pick random
   * port.
   *
   * @return WireMock server port
   */
  int port() default 0;

  /**
   * The name of WireMock server.
   *
   * @return the name of WireMock server.
   */
  String name() default "wiremock";

  /**
   * If the port property is found in Spring {@link org.springframework.context.ApplicationContext}
   * it will be used. Enables a user to predefine a static port with a property.
   *
   * @return true if enabled, else false.
   */
  boolean usePortFromPredefinedPropertyIfFound() default false;

  /**
   * Names of Spring properties to inject the {@link WireMockServer#port()}
   *
   * @return names of Spring properties to inject the {@link WireMockServer#port()}
   */
  String[] portProperties() default {"wiremock.server.port"};

  /**
   * Names of Spring properties to inject the {@link WireMockServer#baseUrl()}.
   *
   * @return names of Spring properties to inject the {@link WireMockServer#baseUrl()}.
   */
  String[] baseUrlProperties() default {"wiremock.server.baseUrl"};

  /**
   * Classpaths to pass to {@link WireMockConfiguration#usingFilesUnderClasspath(String)}. First one
   * that is found will be used. If a {@link #name()} is supplied, it will first look for {@link
   * #filesUnderClasspath()}/{@link #name()} enabling different mappings for differently named
   * WireMocks.
   */
  String[] filesUnderClasspath() default {"wiremock", "stubs", "mappings"};

  /**
   * Directory paths to pass to {@link WireMockConfiguration#usingFilesUnderDirectory(String)}.
   * First one that is found will be used. If a {@link #name()} is supplied, it will first look for
   * {@link #filesUnderClasspath()}/{@link #name()} enabling different mappings for differently
   * named WireMocks.
   */
  String[] filesUnderDirectory() default {"wiremock", "stubs", "mappings"};

  /**
   * WireMock extensions to register in {@link WireMockServer}.
   *
   * @return the extensions
   */
  Class<? extends Extension>[] extensions() default {};

  /**
   * Customizes {@link WireMockConfiguration} used by {@link WireMockServer} instance. Customizers
   * are ordered by their natural order in this array. Each customizer must have no-arg constructor.
   *
   * @return the configuration customizers classes
   */
  Class<? extends WireMockConfigurationCustomizer>[] configurationCustomizers() default {};
}
