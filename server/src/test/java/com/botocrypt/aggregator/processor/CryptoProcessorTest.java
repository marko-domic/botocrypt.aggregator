package com.botocrypt.aggregator.processor;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CryptoProcessorTest {

  @Mock
  private ExchangeProcessor exchangeProcessor;

  @Test
  void testProcessCryptoFromExchanges() {

    final List<ExchangeProcessor> exchangeProcessors = Collections.singletonList(exchangeProcessor);
    final CryptoProcessor cryptoProcessor = new CryptoProcessor(exchangeProcessors);

    doReturn(null).when(exchangeProcessor).getCoinPrices();

    cryptoProcessor.processCryptoFromExchanges();

    verify(exchangeProcessor).getCoinPrices();
  }

  @Test
  void testProcessCryptoFromExchangesWithException() {

    final List<ExchangeProcessor> exchangeProcessors = Collections.singletonList(exchangeProcessor);
    final CryptoProcessor cryptoProcessor = new CryptoProcessor(exchangeProcessors);
    final String exceptionMessage = "Exception on getCoinPrices method call";

    doThrow(new RuntimeException(exceptionMessage)).when(exchangeProcessor).getCoinPrices();

    final Exception exception = assertThrows(RuntimeException.class,
        cryptoProcessor::processCryptoFromExchanges);

    assertEquals(exceptionMessage, exception.getMessage());
    verify(exchangeProcessor).getCoinPrices();
  }
}
