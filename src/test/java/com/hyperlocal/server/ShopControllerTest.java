package com.hyperlocal.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;

@SpringBootTest
public class ShopControllerTest {

  @Mock
  PubSubTemplate template;

  @Spy
  ShopController controller = new ShopController(template);

  @Test
  public void shouldInsertShop() throws Exception {
    String TEST_URL = "{\"ShopID\":3,\"MerchantID\": \"4\",\"ShopName\":\"Test Shop\", \"Latitude\": 23.2323,\"Longitude\":23.53656, \"AddressLine1\":\"S-12\", \"TypeOfService\":\"Test\"}";
    CompletableFuture<String> future = CompletableFuture.completedFuture(TEST_URL);
    controller.insertShop(TEST_URL);
    verify(controller).insertShop(TEST_URL);
  }

}