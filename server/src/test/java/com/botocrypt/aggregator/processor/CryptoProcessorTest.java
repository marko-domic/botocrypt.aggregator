package com.botocrypt.aggregator.processor;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.botocrypt.aggregator.model.CryptoPairOrder;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CryptoProcessorTest {

  @Test
  void testProcessCryptoFromExchanges() {

    final String exchangeName1 = "exchange1";
    final String exchangeName2 = "exchange2";

    final CryptoPairOrder pairOrder1 = new CryptoPairOrder("BTC", "USD",
        BigDecimal.valueOf(54541.17647058824), BigDecimal.valueOf(0.17),
        BigDecimal.valueOf(54843.75), BigDecimal.valueOf(0.16), exchangeName1);
    final CryptoPairOrder pairOrder2 = new CryptoPairOrder("ETH", "USD",
        BigDecimal.valueOf(2545.3), BigDecimal.valueOf(6.5),
        BigDecimal.valueOf(2547.5), BigDecimal.valueOf(6.3), exchangeName1);
    final CryptoPairOrder pairOrder3 = new CryptoPairOrder("BTC", "USD",
        BigDecimal.valueOf(54542.37), BigDecimal.valueOf(0.15),
        BigDecimal.valueOf(54844.63), BigDecimal.valueOf(0.17), exchangeName1);
    final CryptoPairOrder pairOrder4 = new CryptoPairOrder("ETH", "USD",
        BigDecimal.valueOf(2547.7), BigDecimal.valueOf(6.2),
        BigDecimal.valueOf(2549.1), BigDecimal.valueOf(6.1), exchangeName1);

    final List<CryptoPairOrder> orders1 = Arrays.asList(pairOrder1, pairOrder2);
    final List<CryptoPairOrder> orders2 = Arrays.asList(pairOrder3, pairOrder4);
    final ExchangeProcessor exchangeProcessor1 = mock(ExchangeProcessor.class);
    final ExchangeProcessor exchangeProcessor2 = mock(ExchangeProcessor.class);

    final List<ExchangeProcessor> exchangeProcessors = Arrays
        .asList(exchangeProcessor1, exchangeProcessor2);
    final CryptoProcessor cryptoProcessor = new CryptoProcessor(exchangeProcessors);

    doReturn(exchangeName1).when(exchangeProcessor1).exchangeName();
    doReturn(exchangeName2).when(exchangeProcessor2).exchangeName();
    doReturn(orders1).when(exchangeProcessor1).getCoinPrices();
    doReturn(orders2).when(exchangeProcessor2).getCoinPrices();

    cryptoProcessor.processCryptoFromExchanges();

    verify(exchangeProcessor1).getCoinPrices();
    verify(exchangeProcessor2).getCoinPrices();
    verify(exchangeProcessor1).exchangeName();
    verify(exchangeProcessor2).exchangeName();
  }

  @Test
  void testProcessCryptoFromExchangesWithException() {

    final ExchangeProcessor exchangeProcessor = mock(ExchangeProcessor.class);
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
