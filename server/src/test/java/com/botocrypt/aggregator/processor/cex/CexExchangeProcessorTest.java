package com.botocrypt.aggregator.processor.cex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.botocrypt.aggregator.model.Coin;
import com.botocrypt.aggregator.model.CoinPair;
import com.botocrypt.aggregator.model.CoinPairIdentity;
import com.botocrypt.aggregator.model.CryptoPairOrder;
import com.botocrypt.aggregator.model.Exchange;
import com.botocrypt.aggregator.service.CoinPairService;
import com.botocrypt.exchange.cex.io.api.OrderBookApi;
import com.botocrypt.exchange.cex.io.dto.OrderBookDto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class CexExchangeProcessorTest {

  private static final String FIRST_MARKET_SYMBOL = "BTC";
  private static final String SECOND_MARKET_SYMBOL = "USD";
  private static final String CEX_EXCHANGE_NAME = "CEX.IO";

  @Mock
  private OrderBookApi orderBookApi;

  @Mock
  private CoinPairService coinPairService;

  private CexExchangeProcessor cexExchangeProcessor;

  @BeforeEach
  void setup() {
    cexExchangeProcessor = new CexExchangeProcessor(orderBookApi, coinPairService,
        CEX_EXCHANGE_NAME);
  }

  @Test
  void testGetCoinPrices() {

    final String marketSymbolPair = "BTC:USD";

    final Exchange exchange = new Exchange();
    exchange.setId(1);
    exchange.setName(CEX_EXCHANGE_NAME);

    final Coin firstCoin = new Coin();
    firstCoin.setId(1);
    firstCoin.setSymbol(FIRST_MARKET_SYMBOL);
    firstCoin.setMinAmount(0.15);

    final Coin secondCoin = new Coin();
    secondCoin.setId(2);
    secondCoin.setSymbol(SECOND_MARKET_SYMBOL);
    secondCoin.setMinAmount(10000);

    final CoinPairIdentity coinPairIdentity = new CoinPairIdentity();
    coinPairIdentity.setFirstCoinId(firstCoin.getId());
    coinPairIdentity.setSecondCoinId(secondCoin.getId());
    coinPairIdentity.setExchangeId(exchange.getId());

    final CoinPair coinPair = new CoinPair();
    coinPair.setFirstCoinId(coinPairIdentity.getFirstCoinId());
    coinPair.setSecondCoinId(coinPairIdentity.getSecondCoinId());
    coinPair.setExchangeId(coinPairIdentity.getExchangeId());
    coinPair.setFirstCoin(firstCoin);
    coinPair.setSecondCoin(secondCoin);
    coinPair.setExchange(exchange);
    coinPair.setMarketSymbol(marketSymbolPair);

    final List<List<BigDecimal>> bids = new ArrayList<>();
    bids.add(Arrays.asList(BigDecimal.valueOf(54700), BigDecimal.valueOf(0.03)));
    bids.add(Arrays.asList(BigDecimal.valueOf(54600), BigDecimal.valueOf(0.07)));
    bids.add(Arrays.asList(BigDecimal.valueOf(54500), BigDecimal.valueOf(0.01)));
    bids.add(Arrays.asList(BigDecimal.valueOf(54400), BigDecimal.valueOf(0.06)));
    bids.add(Arrays.asList(BigDecimal.valueOf(54300), BigDecimal.valueOf(0.02)));

    final List<List<BigDecimal>> asks = new ArrayList<>();
    asks.add(Arrays.asList(BigDecimal.valueOf(54700), BigDecimal.valueOf(0.04)));
    asks.add(Arrays.asList(BigDecimal.valueOf(54800), BigDecimal.valueOf(0.05)));
    asks.add(Arrays.asList(BigDecimal.valueOf(54900), BigDecimal.valueOf(0.03)));
    asks.add(Arrays.asList(BigDecimal.valueOf(55000), BigDecimal.valueOf(0.04)));
    asks.add(Arrays.asList(BigDecimal.valueOf(55100), BigDecimal.valueOf(0.02)));

    final OrderBookDto orderBookDto = new OrderBookDto();
    orderBookDto.setBids(bids);
    orderBookDto.setAsks(asks);
    orderBookDto.setPair(marketSymbolPair);

    final Mono<OrderBookDto> monoResponse = Mono.just(orderBookDto);

    doReturn(Collections.singletonList(coinPair)).when(coinPairService)
        .getCoinPairsFromRepository(eq(CEX_EXCHANGE_NAME));
    doReturn(monoResponse).when(orderBookApi)
        .getOrderBookForCryptoPair(eq(FIRST_MARKET_SYMBOL), eq(SECOND_MARKET_SYMBOL));

    final List<CryptoPairOrder> cryptoPairOrders = cexExchangeProcessor.getCoinPrices();

    assertFalse(cryptoPairOrders.isEmpty());
    assertEquals(1, cryptoPairOrders.size());

    final CryptoPairOrder cryptoPairOrder = cryptoPairOrders.get(0);
    assertEquals(BigDecimal.valueOf(54541.17647058824), cryptoPairOrder.getBidAveragePrice());
    assertEquals(BigDecimal.valueOf(0.17), cryptoPairOrder.getBidQuantity());
    assertEquals(BigDecimal.valueOf(54843.75), cryptoPairOrder.getAskAveragePrice());
    assertEquals(BigDecimal.valueOf(0.16), cryptoPairOrder.getAskQuantity());
    verify(coinPairService).getCoinPairsFromRepository(eq(CEX_EXCHANGE_NAME));
    verify(orderBookApi)
        .getOrderBookForCryptoPair(eq(FIRST_MARKET_SYMBOL), eq(SECOND_MARKET_SYMBOL));
  }

  @Test
  void testGetCoinPricesWithExceptionOnExchangeRepository() {

    final String exceptionMessage = "Exception on findOneByName method call";

    doThrow(new RuntimeException(exceptionMessage)).when(coinPairService)
        .getCoinPairsFromRepository(eq(CEX_EXCHANGE_NAME));

    final Exception exception = assertThrows(RuntimeException.class,
        () -> cexExchangeProcessor.getCoinPrices());

    assertEquals(exceptionMessage, exception.getMessage());
    verify(coinPairService).getCoinPairsFromRepository(eq(CEX_EXCHANGE_NAME));
  }
}
