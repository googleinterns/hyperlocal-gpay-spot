package com.hyperlocal.server;

import static org.mockito.Mockito.verify;

import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.assertj.core.api.Assertions.assertThat;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;

@SpringBootTest
public class ShopControllerTest {

  private final String SHOP_DATA_AS_STRING = "{\"ShopID\":3,\"MerchantID\": \"4\",\"ShopName\":\"Test Shop\", \"Latitude\": 23.2323,\"Longitude\":23.53656, \"AddressLine1\":\"S-12\", \"TypeOfService\":\"Test\"}";
 
  @Mock
  PubSubTemplate template;

  @Spy
  ShopController controller = new ShopController(template);

  @Test
  public void shouldInsertShop() throws Exception {
    assertThat(controller).isNotNull();
    assertThat(SHOP_DATA_AS_STRING).isNotNull();
    controller.insertShop(SHOP_DATA_AS_STRING);
    verify(controller).insertNewShop(JsonParser.parseString(SHOP_DATA_AS_STRING).getAsJsonObject());
  }

  @Test
  public void shouldUpdateShop() throws Exception {
    assertThat(controller).isNotNull();
    assertThat(SHOP_DATA_AS_STRING).isNotNull();
    controller.updateShop(SHOP_DATA_AS_STRING);
    verify(controller).updateShopDetails(JsonParser.parseString(SHOP_DATA_AS_STRING).getAsJsonObject());
  }
}