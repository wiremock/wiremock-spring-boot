package org.wiremock.spring;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.ExtensionFactory;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Configures WireMock instance.
 *
 * @author Maciej Walkowiak
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigureWireMock {
  public static final List<String> DEFAULT_FILES_UNDER_DIRECTORY =
      List.of(
          "wiremock",
          "stubs",
          "mappings",
          "src/test/resources/wiremock",
          "src/test/resources/stubs",
          "src/test/resources/mappings",
          "src/integtest/resources/wiremock",
          "src/integtest/resources/stubs",
          "src/integtest/resources/mappings");

  /**
   * Port on which WireMock server is going to listen.
   *
   * <p>{@code -1} means disabled.
   *
   * <p>{@code 0} means WireMock will pick random available port.
   *
   * <p>{@code >0} means that static port will be used. A static port will, by default, make Spring
   * context dirty to avoid port collisions. Automatic dirty context can be turned off with {@link
   * #staticPortDirtySpringContext()}.
   *
   * @return WireMock server port
   */
  int port() default 0;

  /**
   * Same as {@link #port()} but for HTTPS.
   *
   * @return HTTPS port to use.
   */
  int httpsPort() default -1;

  /**
   * If you are having performance problems, you may want to set this to false. Making context dirty
   * is intended as a fix to avoid port collisions when using static port.
   *
   * @return true if a static port should make Spring context dirty.
   */
  boolean staticPortDirtySpringContext() default true;

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
   * Names of Spring properties to inject the {@link WireMockServer#httpsPort()}
   *
   * @return names of Spring properties to inject the {@link WireMockServer#httpsPort()}
   */
  String[] httpsPortProperties() default {"wiremock.server.httpsPort"};

  /**
   * Names of Spring properties to inject the {@link WireMockServer#baseUrl()}.
   *
   * @return names of Spring properties to inject the {@link WireMockServer#baseUrl()}.
   */
  String[] baseUrlProperties() default {"wiremock.server.baseUrl"};

  /**
   * Names of Spring properties to inject the {@link WireMockServer#baseUrl()}.
   *
   * @return names of Spring properties to inject the {@link WireMockServer#baseUrl()}.
   */
  String[] httpsBaseUrlProperties() default {"wiremock.server.httpsBaseUrl"};

  /**
   * Classpaths to pass to {@link WireMockConfiguration#usingFilesUnderClasspath(String)}. See also
   * {@link #filesUnderDirectory()}.
   */
  String filesUnderClasspath() default "";

  /**
   * Directory paths to pass to {@link WireMockConfiguration#usingFilesUnderDirectory(String)}.
   * First existing directory will be used if list is given.
   *
   * <p>It will search for mocks in this order:
   * <li>In filesystem {@link #filesUnderDirectory()}
   * <li>In classpath {@link #filesUnderClasspath()}
   * <li>In filesystem {@link #DEFAULT_FILES_UNDER_DIRECTORY}
   */
  String[] filesUnderDirectory() default {};

  /**
   * WireMock extensions to register in {@link WireMockServer}.
   *
   * @return the extensions
   */
  Class<? extends Extension>[] extensions() default {};

  /**
   * WireMock extensions to register in {@link WireMockServer}.
   *
   * @return the extensions
   */
  Class<? extends ExtensionFactory>[] extensionFactories() default {};

  /**
   * Customizes {@link WireMockConfiguration} used by {@link WireMockServer} instance. Customizers
   * are ordered by their natural order in this array. Each customizer must have no-arg constructor.
   *
   * @return the configuration customizers classes
   */
  Class<? extends WireMockConfigurationCustomizer>[] configurationCustomizers() default {};

  /**
   * When tests are running concurrently they will break each other if servers are being reset
   * between tests. Automatic reset is turned on by default, this option allows a user to turn it
   * off.
   *
   * @return true if {@link WireMockServer} should be invoked with {@link WireMockServer#resetAll()}
   *     between test runs.
   */
  boolean resetWireMockServer() default true;

  /**
   * If <code>true</code>, it will register {@link WireMockServer} as a Spring Bean so that it can
   * be {@link Autowired} by name.
   */
  boolean registerSpringBean() default false;

  /**
   * @see WireMockConfiguration#globalTemplating(boolean)
   */
  boolean globalTemplating() default false;
}
