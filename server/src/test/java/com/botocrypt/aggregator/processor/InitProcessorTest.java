package com.botocrypt.aggregator.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.botocrypt.aggregator.service.CoinInitService;
import com.botocrypt.aggregator.service.CoinPairInitService;
import com.botocrypt.aggregator.service.ExchangeInitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class InitProcessorTest {

  @Mock
  private CoinInitService coinInitService;

  @Mock
  private ExchangeInitService exchangeInitService;

  @Mock
  private CoinPairInitService coinPairInitService;

  private InitProcessor initProcessor;

  @BeforeEach
  void setup() {
    initProcessor = new InitProcessor(coinInitService, exchangeInitService, coinPairInitService);
  }

  @Test
  void testInitRepositoryWithNecessaryData() {
    doNothing().when(coinInitService).init();
    doNothing().when(exchangeInitService).init();
    doNothing().when(coinPairInitService).init();

    initProcessor.initRepositoryWithNecessaryData();

    verify(coinInitService).init();
    verify(exchangeInitService).init();
    verify(coinPairInitService).init();
  }

  @Test
  void testInitRepositoryWithException() {
    final String errorMessage = "Exception on init method call.";

    doThrow(new RuntimeException(errorMessage)).when(coinInitService).init();

    Exception e = assertThrows(RuntimeException.class,
        () -> initProcessor.initRepositoryWithNecessaryData());

    assertEquals(errorMessage, e.getMessage());
    verify(coinInitService).init();
    verify(exchangeInitService, times(0)).init();
    verify(coinPairInitService, times(0)).init();
  }
}
