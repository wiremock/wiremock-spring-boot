package org.wiremock.spring.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

class StoreTest {
  @Test
  void removesClosedContextFromStore() throws Exception {
    GenericApplicationContext context = new GenericApplicationContext();
    WireMockServer server = new WireMockServer();
    Store.INSTANCE.store(context, "test", server);
    assertSame(server, Store.INSTANCE.findWireMockInstance(context, "test"));

    Store.INSTANCE.remove(context);

    Field field = Store.class.getDeclaredField("store");
    field.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<ApplicationContext, ?> entries = (Map<ApplicationContext, ?>) field.get(Store.INSTANCE);
    assertFalse(entries.containsKey(context));
    context.close();
  }
}
