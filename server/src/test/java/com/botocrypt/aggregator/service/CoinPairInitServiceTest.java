package com.botocrypt.aggregator.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.botocrypt.aggregator.model.Coin;
import com.botocrypt.aggregator.model.CoinPair;
import com.botocrypt.aggregator.model.CoinPairIdentity;
import com.botocrypt.aggregator.model.Exchange;
import com.botocrypt.aggregator.repository.CoinPairRepository;
import com.botocrypt.aggregator.repository.CoinRepository;
import com.botocrypt.aggregator.repository.ExchangeRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CoinPairInitServiceTest {

  private static final Coin[] COINS = {
      Coin.builder().id(1).symbol("BTC").minAmount(0.15944).build(),
      Coin.builder().id(2).symbol("USD").minAmount(10000).build()
  };

  private static final Exchange[] EXCHANGES = {
      Exchange.builder().id(1).name("CEX.IO").build()
  };

  private static final CoinPairIdentity[] COIN_PAIR_IDENTITIES = {
      CoinPairIdentity.builder().firstCoinId(1).secondCoinId(2).exchangeId(1).build()
  };

  private static final CoinPair[] COIN_PAIRS = {
      CoinPair.builder().firstCoinId(COIN_PAIR_IDENTITIES[0].getFirstCoinId())
          .secondCoinId(COIN_PAIR_IDENTITIES[0].getSecondCoinId())
          .exchangeId(COIN_PAIR_IDENTITIES[0].getExchangeId()).firstCoin(COINS[0])
          .secondCoin(COINS[1]).exchange(EXCHANGES[0]).marketSymbol("BTC:USD").build()
  };

  @Mock
  private CoinRepository coinRepository;

  @Mock
  private ExchangeRepository exchangeRepository;

  @Mock
  private CoinPairRepository coinPairRepository;

  @InjectMocks
  private CoinPairInitService coinPairInitService;

  @Test
  void testInit() {

    doReturn(Arrays.asList(COINS)).when(coinRepository)
        .findBySymbolIn(eq(Arrays.asList("BTC", "USD")));
    doReturn(Arrays.asList(EXCHANGES)).when(exchangeRepository)
        .findByNameIn(eq(Collections.singletonList("CEX.IO")));
    Arrays.stream(COIN_PAIR_IDENTITIES).forEach(
        pair_id -> doReturn(Optional.empty()).when(coinPairRepository).findById(eq(pair_id)));
    Arrays.stream(COIN_PAIRS)
        .forEach(pair -> doReturn(pair).when(coinPairRepository).saveAndFlush(any(CoinPair.class)));

    coinPairInitService.init();

    verify(coinRepository).findBySymbolIn(eq(Arrays.asList("BTC", "USD")));
    verify(exchangeRepository).findByNameIn(eq(Collections.singletonList("CEX.IO")));
    Arrays.stream(COIN_PAIR_IDENTITIES)
        .forEach(pair_id -> verify(coinPairRepository).findById(eq(pair_id)));
    verify(coinPairRepository, times(COIN_PAIRS.length)).saveAndFlush(any(CoinPair.class));
  }

  @Test
  void testInitIfCoinPairsAreAlreadyInRepository() {

    doReturn(Arrays.asList(COINS)).when(coinRepository)
        .findBySymbolIn(eq(Arrays.asList("BTC", "USD")));
    doReturn(Arrays.asList(EXCHANGES)).when(exchangeRepository)
        .findByNameIn(eq(Collections.singletonList("CEX.IO")));
    for (int i = 0; i < COIN_PAIRS.length; i++) {
      doReturn(Optional.of(COIN_PAIRS[i])).when(coinPairRepository)
          .findById(eq(COIN_PAIR_IDENTITIES[i]));
    }

    coinPairInitService.init();

    verify(coinRepository).findBySymbolIn(eq(Arrays.asList("BTC", "USD")));
    verify(exchangeRepository).findByNameIn(eq(Collections.singletonList("CEX.IO")));
    Arrays.stream(COIN_PAIR_IDENTITIES)
        .forEach(pair_id -> verify(coinPairRepository).findById(eq(pair_id)));
    verify(coinPairRepository, times(0)).saveAndFlush(any(CoinPair.class));
  }
}
