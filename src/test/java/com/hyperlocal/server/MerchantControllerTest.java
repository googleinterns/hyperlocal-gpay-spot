package com.hyperlocal.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MerchantControllerTest {

  Merchant merchant = new Merchant(4L,"Test Merchant", "7867986767");
  private final String MERCHANT_DATA_AS_STRING = new Gson().toJson(merchant);
  
  @Spy
  MerchantController controller = new MerchantController();

  @Test
  public void shouldInsertMerchant() throws Exception {
    assertThat(controller).isNotNull();
    assertThat(MERCHANT_DATA_AS_STRING).isNotNull();
    controller.updateMerchant(MERCHANT_DATA_AS_STRING);
    verify(controller).updateMerchantDetails(JsonParser.parseString(MERCHANT_DATA_AS_STRING).getAsJsonObject());
  }

  @Test
  public void shouldUpdateMerchant() throws Exception {
    assertThat(controller).isNotNull();
    assertThat(MERCHANT_DATA_AS_STRING).isNotNull();
    controller.updateMerchant(MERCHANT_DATA_AS_STRING);
    verify(controller).updateMerchantDetails(JsonParser.parseString(MERCHANT_DATA_AS_STRING).getAsJsonObject());
  }
}