package com.hyperlocal.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UtilitiesTest {

  @InjectMocks
  Utilities util = new Utilities();
  
  @Test
  public void shouldCompareDoubleThreshold() throws Exception {
    // ARRANGE
    boolean expectedOutput1 = true;
    boolean expectedOutput2 = true;
    boolean expectedOutput3 = false;
    boolean expectedOutput4 = false;
    
    
    // ACT
    boolean actualOutput1 = Utilities.doubleThresholdCompare(5d, 5.55d, 0.56d);
    boolean actualOutput2 = Utilities.doubleThresholdCompare(20d, -20d, 50d);
    boolean actualOutput3 = Utilities.doubleThresholdCompare(5d, 5.55d, 0.54d);
    boolean actualOutput4 = Utilities.doubleThresholdCompare(20d, -20d, 39d);
    
    
    // ASSERT
    assertEquals(expectedOutput1, actualOutput1);
    assertEquals(expectedOutput2, actualOutput2);
    assertEquals(expectedOutput3, actualOutput3);
    assertEquals(expectedOutput4, actualOutput4);
  }

  @Test
  public void shouldGetPlaceHolderString() throws Exception {
    // ARRANGE
    String expectedOutput1 = "";
    String expectedOutput2 = "?";
    String expectedOutput3 = "?,?";
    String expectedOutput4 = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
    
    
    // ACT
    String actualOutput1 = Utilities.getPlaceHolderString(0);
    String actualOutput2 = Utilities.getPlaceHolderString(1);
    String actualOutput3 = Utilities.getPlaceHolderString(2);
    String actualOutput4 = Utilities.getPlaceHolderString(28);
    
    
    // ASSERT
    assertEquals(expectedOutput1, actualOutput1);
    assertEquals(expectedOutput2, actualOutput2);
    assertEquals(expectedOutput3, actualOutput3);
    assertEquals(expectedOutput4, actualOutput4);
  }
}