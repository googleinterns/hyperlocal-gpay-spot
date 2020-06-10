package com.hyperlocal.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class HyperlocalApplicationTests {

  @Autowired
  private MerchantController merchantController;

  @Autowired
  private ShopController shopController;

  @Test
  void contextLoads() {
    System.out.println("Context loads running");
    assertThat(merchantController).isNotNull();
    assertThat(shopController).isNotNull();
  }
}
