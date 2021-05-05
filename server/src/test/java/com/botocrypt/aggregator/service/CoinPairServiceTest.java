package com.botocrypt.aggregator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.botocrypt.aggregator.model.Coin;
import com.botocrypt.aggregator.model.CoinPair;
import com.botocrypt.aggregator.model.Exchange;
import com.botocrypt.aggregator.repository.CoinPairRepository;
import com.botocrypt.aggregator.repository.ExchangeRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CoinPairServiceTest {

  private static final String EXCHANGE = "CEX.IO";

  @Mock
  private ExchangeRepository exchangeRepository;

  @Mock
  private CoinPairRepository coinPairRepository;

  @InjectMocks
  private CoinPairService coinPairService;

  @Test
  void testGetCoinPairsFromRepository() {
    final Exchange exchange = Exchange.builder().id(1).name(EXCHANGE).build();
    final Coin btcCoin = Coin.builder().id(1).symbol("BTC").minAmount(0.15).build();
    final Coin usdCoin = Coin.builder().id(2).symbol("USD").minAmount(10000).build();
    final CoinPair coinPair = CoinPair.builder()
        .firstCoinId(1)
        .secondCoinId(2)
        .exchangeId(1)
        .firstCoin(btcCoin)
        .secondCoin(usdCoin)
        .exchange(exchange)
        .marketSymbol("BTC:USD")
        .build();

    doReturn(exchange).when(exchangeRepository).findOneByName(eq(EXCHANGE));
    doReturn(Collections.singletonList(coinPair)).when(coinPairRepository)
        .findByExchangeId(eq(1));

    List<CoinPair> coinPairs = coinPairService.getCoinPairsFromRepository(EXCHANGE);

    assertNotNull(coinPairs);
    assertFalse(coinPairs.isEmpty());
    assertEquals(1, coinPairs.size());
    assertEquals(coinPair, coinPairs.get(0));
    verify(exchangeRepository).findOneByName(eq(EXCHANGE));
    verify(coinPairRepository).findByExchangeId(eq(1));
  }

  @Test
  void testGetCoinPairsIfNoExchangeInRepository() {

    doReturn(null).when(exchangeRepository).findOneByName(eq(EXCHANGE));

    List<CoinPair> coinPairs = coinPairService.getCoinPairsFromRepository(EXCHANGE);

    assertNotNull(coinPairs);
    assertTrue(coinPairs.isEmpty());
    verify(exchangeRepository).findOneByName(eq(EXCHANGE));
    verify(coinPairRepository, times(0)).findByExchangeId(eq(1));
  }

  @Test
  void testGetCoinPairsIfNoPairsInRepository() {
    final Exchange exchange = Exchange.builder().id(1).name(EXCHANGE).build();

    doReturn(exchange).when(exchangeRepository).findOneByName(eq(EXCHANGE));
    doReturn(Collections.emptyList()).when(coinPairRepository)
        .findByExchangeId(eq(1));

    List<CoinPair> coinPairs = coinPairService.getCoinPairsFromRepository(EXCHANGE);

    assertNotNull(coinPairs);
    assertTrue(coinPairs.isEmpty());
    verify(exchangeRepository).findOneByName(eq(EXCHANGE));
    verify(coinPairRepository).findByExchangeId(eq(1));
  }
}
