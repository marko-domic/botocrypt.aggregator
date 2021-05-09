package com.botocrypt.aggregator.processor;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.botocrypt.aggregator.model.CryptoPairOrder;
import com.botocrypt.arbitrage.api.Arbitrage.CoinPairDto;
import com.botocrypt.arbitrage.api.ArbitrageServiceGrpc.ArbitrageServiceStub;
import io.grpc.stub.StreamObserver;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
public class CryptoProcessorTest {

  @Test
  void testProcessCryptoFromExchanges() throws InterruptedException {

    final String exchangeName1 = "exchange1";
    final String exchangeName2 = "exchange2";
    final String cycleId = UUID.randomUUID().toString();

    final CryptoPairOrder pairOrder1 = new CryptoPairOrder("BTC", "USD",
        BigDecimal.valueOf(54541.17647058824), BigDecimal.valueOf(0.17),
        BigDecimal.valueOf(54843.75), BigDecimal.valueOf(0.16), exchangeName1);
    final CryptoPairOrder pairOrder2 = new CryptoPairOrder("ETH", "USD",
        BigDecimal.valueOf(2545.3), BigDecimal.valueOf(6.5),
        BigDecimal.valueOf(2547.5), BigDecimal.valueOf(6.3), exchangeName1);
    final CryptoPairOrder pairOrder3 = new CryptoPairOrder("BTC", "USD",
        BigDecimal.valueOf(54542.37), BigDecimal.valueOf(0.15),
        BigDecimal.valueOf(54844.63), BigDecimal.valueOf(0.17), exchangeName2);
    final CryptoPairOrder pairOrder4 = new CryptoPairOrder("ETH", "USD",
        BigDecimal.valueOf(2547.7), BigDecimal.valueOf(6.2),
        BigDecimal.valueOf(2549.1), BigDecimal.valueOf(6.1), exchangeName2);

    final List<CryptoPairOrder> orders1 = Arrays.asList(pairOrder1, pairOrder2);
    final List<CryptoPairOrder> orders2 = Arrays.asList(pairOrder3, pairOrder4);
    final ExchangeProcessor exchangeProcessor1 = mock(ExchangeProcessor.class);
    final ExchangeProcessor exchangeProcessor2 = mock(ExchangeProcessor.class);
    final ObjectProvider<ArbitrageServiceStub> arbitrageServiceClientProvider = mock(
        ObjectProvider.class);
    final ArbitrageServiceStub arbitrageServiceClient = mock(ArbitrageServiceStub.class);
    final ObjectProvider<ArbitrageResponseObserver> arbitrageResponseObserverProvider = mock(
        ObjectProvider.class);
    final ArbitrageResponseObserver arbitrageResponseObserver = mock(
        ArbitrageResponseObserver.class);
    final StreamObserver<CoinPairDto> arbitrageRequestObserver = mock(StreamObserver.class);
    final Supplier<String> cycleIdSupplier = mock(Supplier.class);

    final List<ExchangeProcessor> exchangeProcessors = Arrays
        .asList(exchangeProcessor1, exchangeProcessor2);
    final CryptoProcessor cryptoProcessor = new CryptoProcessor(exchangeProcessors,
        arbitrageServiceClientProvider, arbitrageResponseObserverProvider, cycleIdSupplier);

    doReturn(cycleId).when(cycleIdSupplier).get();
    doReturn(arbitrageServiceClient).when(arbitrageServiceClientProvider).getObject();
    doReturn(arbitrageResponseObserver).when(arbitrageResponseObserverProvider).getObject();
    doReturn(arbitrageRequestObserver).when(arbitrageServiceClient)
        .sendCoinPairInfoFromExchange(eq(arbitrageResponseObserver));
    doNothing().when(arbitrageRequestObserver).onNext(any(CoinPairDto.class));
    doReturn(orders1).when(exchangeProcessor1).getCoinPrices();
    doReturn(orders2).when(exchangeProcessor2).getCoinPrices();
    doNothing().when(arbitrageResponseObserver).waitForResponseToComplete();

    cryptoProcessor.processCryptoFromExchanges();

    verify(cycleIdSupplier).get();
    verify(arbitrageServiceClientProvider).getObject();
    verify(arbitrageResponseObserverProvider).getObject();
    verify(arbitrageServiceClient).sendCoinPairInfoFromExchange(eq(arbitrageResponseObserver));
    verify(exchangeProcessor1).getCoinPrices();
    verify(exchangeProcessor2).getCoinPrices();
    verify(arbitrageRequestObserver, times(4)).onNext(any(CoinPairDto.class));
    verify(arbitrageResponseObserver).waitForResponseToComplete();
  }

  @Test
  void testProcessCryptoFromExchangesWithException() {

    final ExchangeProcessor exchangeProcessor = mock(ExchangeProcessor.class);
    final ObjectProvider<ArbitrageServiceStub> arbitrageServiceClientProvider = mock(
        ObjectProvider.class);
    final ObjectProvider<ArbitrageResponseObserver> arbitrageResponseObserverProvider = mock(
        ObjectProvider.class);
    final Supplier<String> cycleIdSupplier = mock(Supplier.class);
    final List<ExchangeProcessor> exchangeProcessors = Collections.singletonList(exchangeProcessor);
    final CryptoProcessor cryptoProcessor = new CryptoProcessor(exchangeProcessors,
        arbitrageServiceClientProvider, arbitrageResponseObserverProvider, cycleIdSupplier);
    final String exceptionMessage = "Exception on get method call";

    doThrow(new RuntimeException(exceptionMessage)).when(cycleIdSupplier).get();

    final Exception exception = assertThrows(RuntimeException.class,
        cryptoProcessor::processCryptoFromExchanges);

    assertEquals(exceptionMessage, exception.getMessage());
    verify(cycleIdSupplier).get();
    verify(arbitrageServiceClientProvider, never()).getObject();
    verify(arbitrageResponseObserverProvider, never()).getObject();
  }

  @Test
  void testProcessCryptoFromExchangesWithExceptionOnWaitForResponse() throws InterruptedException {

    final String exchangeName1 = "exchange1";
    final String exchangeName2 = "exchange2";
    final String cycleId = UUID.randomUUID().toString();

    final CryptoPairOrder pairOrder1 = new CryptoPairOrder("BTC", "USD",
        BigDecimal.valueOf(54541.17647058824), BigDecimal.valueOf(0.17),
        BigDecimal.valueOf(54843.75), BigDecimal.valueOf(0.16), exchangeName1);
    final CryptoPairOrder pairOrder2 = new CryptoPairOrder("ETH", "USD",
        BigDecimal.valueOf(2545.3), BigDecimal.valueOf(6.5),
        BigDecimal.valueOf(2547.5), BigDecimal.valueOf(6.3), exchangeName1);
    final CryptoPairOrder pairOrder3 = new CryptoPairOrder("BTC", "USD",
        BigDecimal.valueOf(54542.37), BigDecimal.valueOf(0.15),
        BigDecimal.valueOf(54844.63), BigDecimal.valueOf(0.17), exchangeName2);
    final CryptoPairOrder pairOrder4 = new CryptoPairOrder("ETH", "USD",
        BigDecimal.valueOf(2547.7), BigDecimal.valueOf(6.2),
        BigDecimal.valueOf(2549.1), BigDecimal.valueOf(6.1), exchangeName2);

    final List<CryptoPairOrder> orders1 = Arrays.asList(pairOrder1, pairOrder2);
    final List<CryptoPairOrder> orders2 = Arrays.asList(pairOrder3, pairOrder4);
    final ExchangeProcessor exchangeProcessor1 = mock(ExchangeProcessor.class);
    final ExchangeProcessor exchangeProcessor2 = mock(ExchangeProcessor.class);
    final ObjectProvider<ArbitrageServiceStub> arbitrageServiceClientProvider = mock(
        ObjectProvider.class);
    final ArbitrageServiceStub arbitrageServiceClient = mock(ArbitrageServiceStub.class);
    final ObjectProvider<ArbitrageResponseObserver> arbitrageResponseObserverProvider = mock(
        ObjectProvider.class);
    final ArbitrageResponseObserver arbitrageResponseObserver = mock(
        ArbitrageResponseObserver.class);
    final StreamObserver<CoinPairDto> arbitrageRequestObserver = mock(StreamObserver.class);
    final Supplier<String> cycleIdSupplier = mock(Supplier.class);

    final List<ExchangeProcessor> exchangeProcessors = Arrays
        .asList(exchangeProcessor1, exchangeProcessor2);
    final CryptoProcessor cryptoProcessor = new CryptoProcessor(exchangeProcessors,
        arbitrageServiceClientProvider, arbitrageResponseObserverProvider, cycleIdSupplier);

    doReturn(cycleId).when(cycleIdSupplier).get();
    doReturn(arbitrageServiceClient).when(arbitrageServiceClientProvider).getObject();
    doReturn(arbitrageResponseObserver).when(arbitrageResponseObserverProvider).getObject();
    doReturn(arbitrageRequestObserver).when(arbitrageServiceClient)
        .sendCoinPairInfoFromExchange(eq(arbitrageResponseObserver));
    doNothing().when(arbitrageRequestObserver).onNext(any(CoinPairDto.class));
    doReturn(orders1).when(exchangeProcessor1).getCoinPrices();
    doReturn(orders2).when(exchangeProcessor2).getCoinPrices();
    doThrow(new InterruptedException("Exception on waitForResponseToComplete method call"))
        .when(arbitrageResponseObserver).waitForResponseToComplete();

    cryptoProcessor.processCryptoFromExchanges();

    verify(cycleIdSupplier).get();
    verify(arbitrageServiceClientProvider).getObject();
    verify(arbitrageResponseObserverProvider).getObject();
    verify(arbitrageServiceClient).sendCoinPairInfoFromExchange(eq(arbitrageResponseObserver));
    verify(exchangeProcessor1).getCoinPrices();
    verify(exchangeProcessor2).getCoinPrices();
    verify(arbitrageRequestObserver, times(4)).onNext(any(CoinPairDto.class));
    verify(arbitrageResponseObserver).waitForResponseToComplete();
  }

  @Test
  void testProcessCryptoFromExchangesWithNoPairOrders() throws InterruptedException {

    final String cycleId = UUID.randomUUID().toString();

    final List<CryptoPairOrder> orders1 = Collections.emptyList();
    final List<CryptoPairOrder> orders2 = Collections.emptyList();
    final ExchangeProcessor exchangeProcessor1 = mock(ExchangeProcessor.class);
    final ExchangeProcessor exchangeProcessor2 = mock(ExchangeProcessor.class);
    final ObjectProvider<ArbitrageServiceStub> arbitrageServiceClientProvider = mock(
        ObjectProvider.class);
    final ArbitrageServiceStub arbitrageServiceClient = mock(ArbitrageServiceStub.class);
    final ObjectProvider<ArbitrageResponseObserver> arbitrageResponseObserverProvider = mock(
        ObjectProvider.class);
    final ArbitrageResponseObserver arbitrageResponseObserver = mock(
        ArbitrageResponseObserver.class);
    final StreamObserver<CoinPairDto> arbitrageRequestObserver = mock(StreamObserver.class);
    final Supplier<String> cycleIdSupplier = mock(Supplier.class);

    final List<ExchangeProcessor> exchangeProcessors = Arrays
        .asList(exchangeProcessor1, exchangeProcessor2);
    final CryptoProcessor cryptoProcessor = new CryptoProcessor(exchangeProcessors,
        arbitrageServiceClientProvider, arbitrageResponseObserverProvider, cycleIdSupplier);

    doReturn(cycleId).when(cycleIdSupplier).get();
    doReturn(arbitrageServiceClient).when(arbitrageServiceClientProvider).getObject();
    doReturn(arbitrageResponseObserver).when(arbitrageResponseObserverProvider).getObject();
    doReturn(arbitrageRequestObserver).when(arbitrageServiceClient)
        .sendCoinPairInfoFromExchange(eq(arbitrageResponseObserver));
    doReturn(orders1).when(exchangeProcessor1).getCoinPrices();
    doReturn(orders2).when(exchangeProcessor2).getCoinPrices();
    doNothing().when(arbitrageResponseObserver).waitForResponseToComplete();

    cryptoProcessor.processCryptoFromExchanges();

    verify(cycleIdSupplier).get();
    verify(arbitrageServiceClientProvider).getObject();
    verify(arbitrageResponseObserverProvider).getObject();
    verify(arbitrageServiceClient).sendCoinPairInfoFromExchange(eq(arbitrageResponseObserver));
    verify(exchangeProcessor1).getCoinPrices();
    verify(exchangeProcessor2).getCoinPrices();
    verify(arbitrageRequestObserver, never()).onNext(any(CoinPairDto.class));
    verify(arbitrageResponseObserver).waitForResponseToComplete();
  }
}
