package com.botocrypt.aggregator.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
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
      Coin.builder().id(2).symbol("USD").minAmount(10000).build(),
      Coin.builder().id(3).symbol("ETH").minAmount(3).build(),
      Coin.builder().id(4).symbol("XRP").minAmount(12286).build(),
      Coin.builder().id(5).symbol("XLM").minAmount(45165).build(),
      Coin.builder().id(6).symbol("LTC").minAmount(83).build(),
      Coin.builder().id(7).symbol("ADA").minAmount(8672).build()
  };

  private static final Exchange[] EXCHANGES = {
      Exchange.builder().id(1).name("CEX.IO").build()
  };

  private static final CoinPairIdentity[] COIN_PAIR_IDENTITIES = {
      CoinPairIdentity.builder().firstCoinId(1).secondCoinId(2).exchangeId(1).build(),
      CoinPairIdentity.builder().firstCoinId(3).secondCoinId(2).exchangeId(1).build(),
      CoinPairIdentity.builder().firstCoinId(3).secondCoinId(1).exchangeId(1).build(),
      CoinPairIdentity.builder().firstCoinId(4).secondCoinId(2).exchangeId(1).build(),
      CoinPairIdentity.builder().firstCoinId(4).secondCoinId(1).exchangeId(1).build(),
      CoinPairIdentity.builder().firstCoinId(5).secondCoinId(2).exchangeId(1).build(),
      CoinPairIdentity.builder().firstCoinId(5).secondCoinId(1).exchangeId(1).build(),
      CoinPairIdentity.builder().firstCoinId(6).secondCoinId(2).exchangeId(1).build(),
      CoinPairIdentity.builder().firstCoinId(6).secondCoinId(1).exchangeId(1).build(),
      CoinPairIdentity.builder().firstCoinId(7).secondCoinId(2).exchangeId(1).build()
  };

  private static final CoinPair[] COIN_PAIRS = {
      CoinPair.builder()
          .id(COIN_PAIR_IDENTITIES[0])
          .firstCoin(COINS[0])
          .secondCoin(COINS[1])
          .exchange(EXCHANGES[0])
          .marketSymbol("BTC:USD")
          .build(),
      CoinPair.builder()
          .id(COIN_PAIR_IDENTITIES[1])
          .firstCoin(COINS[2])
          .secondCoin(COINS[1])
          .exchange(EXCHANGES[0])
          .marketSymbol("ETH:USD")
          .build(),
      CoinPair.builder()
          .id(COIN_PAIR_IDENTITIES[2])
          .firstCoin(COINS[2])
          .secondCoin(COINS[0])
          .exchange(EXCHANGES[0])
          .marketSymbol("ETH:BTC")
          .build(),
      CoinPair.builder()
          .id(COIN_PAIR_IDENTITIES[3])
          .firstCoin(COINS[3])
          .secondCoin(COINS[1])
          .exchange(EXCHANGES[0])
          .marketSymbol("ETH:BTC")
          .build(),
      CoinPair.builder()
          .id(COIN_PAIR_IDENTITIES[4])
          .firstCoin(COINS[3])
          .secondCoin(COINS[0])
          .exchange(EXCHANGES[0])
          .marketSymbol("ETH:BTC")
          .build(),
      CoinPair.builder()
          .id(COIN_PAIR_IDENTITIES[5])
          .firstCoin(COINS[4])
          .secondCoin(COINS[1])
          .exchange(EXCHANGES[0])
          .marketSymbol("ETH:BTC")
          .build(),
      CoinPair.builder()
          .id(COIN_PAIR_IDENTITIES[6])
          .firstCoin(COINS[4])
          .secondCoin(COINS[0])
          .exchange(EXCHANGES[0])
          .marketSymbol("ETH:BTC")
          .build(),
      CoinPair.builder()
          .id(COIN_PAIR_IDENTITIES[7])
          .firstCoin(COINS[5])
          .secondCoin(COINS[1])
          .exchange(EXCHANGES[0])
          .marketSymbol("ETH:BTC")
          .build(),
      CoinPair.builder()
          .id(COIN_PAIR_IDENTITIES[8])
          .firstCoin(COINS[5])
          .secondCoin(COINS[0])
          .exchange(EXCHANGES[0])
          .marketSymbol("ETH:BTC")
          .build(),
      CoinPair.builder()
          .id(COIN_PAIR_IDENTITIES[9])
          .firstCoin(COINS[6])
          .secondCoin(COINS[1])
          .exchange(EXCHANGES[0])
          .marketSymbol("ETH:BTC")
          .build()
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
        .findBySymbolIn(eq(Arrays.asList("BTC", "USD", "ETH", "XRP", "XLM", "LTC", "ADA")));
    doReturn(Arrays.asList(EXCHANGES)).when(exchangeRepository)
        .findByNameIn(eq(Collections.singletonList("CEX.IO")));
    Arrays.stream(COIN_PAIR_IDENTITIES).forEach(
        pair_id -> doReturn(Optional.empty()).when(coinPairRepository).findById(eq(pair_id)));
    doReturn(null).when(coinPairRepository).saveAndFlush(any(CoinPair.class));

    coinPairInitService.init();

    verify(coinRepository).findBySymbolIn(eq(Arrays.asList("BTC", "USD", "ETH", "XRP", "XLM", "LTC", "ADA")));
    verify(exchangeRepository).findByNameIn(eq(Collections.singletonList("CEX.IO")));
    Arrays.stream(COIN_PAIR_IDENTITIES)
        .forEach(pair_id -> verify(coinPairRepository).findById(eq(pair_id)));
    verify(coinPairRepository, times(COIN_PAIRS.length)).saveAndFlush(any(CoinPair.class));
  }

  @Test
  void testInitIfCoinPairsAreAlreadyInRepository() {

    doReturn(Arrays.asList(COINS)).when(coinRepository)
        .findBySymbolIn(eq(Arrays.asList("BTC", "USD", "ETH", "XRP", "XLM", "LTC", "ADA")));
    doReturn(Arrays.asList(EXCHANGES)).when(exchangeRepository)
        .findByNameIn(eq(Collections.singletonList("CEX.IO")));
    for (int i = 0; i < COIN_PAIRS.length; i++) {
      doReturn(Optional.of(COIN_PAIRS[i])).when(coinPairRepository)
          .findById(eq(COIN_PAIR_IDENTITIES[i]));
    }

    coinPairInitService.init();

    verify(coinRepository).findBySymbolIn(eq(Arrays.asList("BTC", "USD", "ETH", "XRP", "XLM", "LTC", "ADA")));
    verify(exchangeRepository).findByNameIn(eq(Collections.singletonList("CEX.IO")));
    Arrays.stream(COIN_PAIR_IDENTITIES)
        .forEach(pair_id -> verify(coinPairRepository).findById(eq(pair_id)));
    verify(coinPairRepository, never()).saveAndFlush(any(CoinPair.class));
  }
}
