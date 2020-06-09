package com.hyperlocal.server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.util.concurrent.CompletableFuture;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

 @SpringBootTest
 @AutoConfigureMockMvc
public class HttpRequestTest {
  
  @Autowired
  private MockMvc mockMvc;
  
  @MockBean
  private ShopController controller;

  @Test
  public void shouldInsertShop() throws Exception {  
    String TEST_URL = "{\"ShopID\":3,\"MerchantID\": \"4\",\"ShopName\":\"Test Shop\", \"Latitude\": 23.2323,\"Longitude\":23.53656, \"AddressLine1\":\"S-12\", \"TypeOfService\":\"Test\"}"; 

    when(controller.insertShop(TEST_URL)).thenReturn(CompletableFuture.completedFuture(TEST_URL));
    
    this.mockMvc.perform(post("/shops/all")
        .contentType(MediaType.APPLICATION_JSON)
        .content(TEST_URL))
        .andDo(print())
    .andExpect(status().isOk());
  }

}