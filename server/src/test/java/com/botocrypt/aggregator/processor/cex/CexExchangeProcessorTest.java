package com.botocrypt.aggregator.processor.cex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.botocrypt.aggregator.model.Coin;
import com.botocrypt.aggregator.model.CoinPair;
import com.botocrypt.aggregator.model.CoinPairIdentity;
import com.botocrypt.aggregator.model.CryptoPairOrder;
import com.botocrypt.aggregator.model.Exchange;
import com.botocrypt.aggregator.repository.CoinPairRepository;
import com.botocrypt.aggregator.repository.ExchangeRepository;
import com.botocrypt.exchange.cex.io.api.OrderbookApi;
import com.botocrypt.exchange.cex.io.dto.OrderbookDto;
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
  private static final String CEX_EXCHANGE_NAME = "CEX.IO";

  @Mock
  private OrderbookApi orderbookApi;

  @Mock
  private ExchangeRepository exchangeRepository;

  @Mock
  private CoinPairRepository coinPairRepository;

  private CexExchangeProcessor cexExchangeProcessor;

  @BeforeEach
  void setup() {
    cexExchangeProcessor = new CexExchangeProcessor(orderbookApi, exchangeRepository,
        coinPairRepository, CEX_EXCHANGE_NAME);
  }

  @Test
  void testGetCoinPrices() {

    final String marketSymbolPair = "BTC:USD";

    final Exchange exchange = new Exchange();
    exchange.setId(1);
    exchange.setName(CEX_EXCHANGE_NAME);

    final Coin firstCoin = new Coin();
    firstCoin.setId(1);
    firstCoin.setSymbol("BTC");
    firstCoin.setMinAmount(0.15);

    final Coin secondCoin = new Coin();
    secondCoin.setId(2);
    secondCoin.setSymbol("USD");
    secondCoin.setMinAmount(10000);

    final CoinPairIdentity coinPairIdentity = new CoinPairIdentity();
    coinPairIdentity.setFirstCoinId(firstCoin.getId());
    coinPairIdentity.setSecondCoinId(secondCoin.getId());
    coinPairIdentity.setExchangeId(exchange.getId());

    final CoinPair coinPair = new CoinPair();
    coinPair.setCoinPairIdentity(coinPairIdentity);
    coinPair.setFirstCoin(firstCoin);
    coinPair.setSecondCoin(secondCoin);
    coinPair.setExchange(exchange);
    coinPair.setMarketSymbol(marketSymbolPair);

    final OrderbookDto orderbookDto = new OrderbookDto();
    orderbookDto.setPair(marketSymbolPair);

    final Mono<OrderbookDto> monoResponse = Mono.just(orderbookDto);

    doReturn(exchange).when(exchangeRepository).findOneByName(eq(CEX_EXCHANGE_NAME));
    doReturn(Collections.singletonList(coinPair)).when(coinPairRepository)
        .findByCoinPairIdentityExchangeId(eq(exchange.getId()));
    doReturn(monoResponse).when(orderbookApi).getOrderbookForCryptoPair(eq("BTC"), eq("USD"));

    final List<CryptoPairOrder> cryptoPairOrders = cexExchangeProcessor.getCoinPrices();

    assertTrue(cryptoPairOrders.isEmpty());
    verify(exchangeRepository).findOneByName(eq(CEX_EXCHANGE_NAME));
    verify(coinPairRepository).findByCoinPairIdentityExchangeId(eq(exchange.getId()));
    verify(orderbookApi).getOrderbookForCryptoPair(eq("BTC"), eq("USD"));
  }

  @Test
  void testGetCoinPricesWithExceptionOnExchangeRepository() {

    final String exceptionMessage = "Exception on findOneByName method call";

    doThrow(new RuntimeException(exceptionMessage)).when(exchangeRepository)
        .findOneByName(eq(CEX_EXCHANGE_NAME));

    final Exception exception = assertThrows(RuntimeException.class,
        () -> cexExchangeProcessor.getCoinPrices());

    assertEquals(exceptionMessage, exception.getMessage());
    verify(exchangeRepository).findOneByName(eq(CEX_EXCHANGE_NAME));
  }
}
