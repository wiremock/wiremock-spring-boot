package com.maciejwalkowiak.wiremock.spring;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigureWiremock {
    int port() default 0;
    String name();
    String property() default "";
}
