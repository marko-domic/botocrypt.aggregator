package com.botocrypt.aggregator.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.botocrypt.aggregator.model.Coin;
import com.botocrypt.aggregator.repository.CoinRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CoinInitServiceTest {

  private static final String BTC = "BTC";
  private static final String USD = "USD";
  private static final double BTC_QUANTITY = 0.15944;
  private static final double USD_QUANTITY = 10000;

  @Mock
  private CoinRepository coinRepository;

  @InjectMocks
  private CoinInitService coinInitService;

  @Test
  void testInit() {
    final Coin btcCoin = Coin.builder()
        .symbol(BTC)
        .minAmount(BTC_QUANTITY)
        .build();
    final Coin usdCoin = Coin.builder()
        .symbol(USD)
        .minAmount(USD_QUANTITY)
        .build();

    doReturn(null).when(coinRepository).findOneBySymbol(eq(BTC));
    doReturn(null).when(coinRepository).findOneBySymbol(eq(USD));
    doReturn(btcCoin).when(coinRepository).saveAndFlush(eq(btcCoin));
    doReturn(usdCoin).when(coinRepository).saveAndFlush(eq(usdCoin));

    coinInitService.init();

    verify(coinRepository).findOneBySymbol(eq(BTC));
    verify(coinRepository).findOneBySymbol(eq(USD));
    verify(coinRepository).saveAndFlush(eq(btcCoin));
    verify(coinRepository).saveAndFlush(eq(usdCoin));
  }

  @Test
  void testInitIfCoinsAreAlreadyInRepository() {
    final Coin btcCoin = Coin.builder()
        .symbol(BTC)
        .minAmount(BTC_QUANTITY)
        .build();
    final Coin usdCoin = Coin.builder()
        .symbol(USD)
        .minAmount(USD_QUANTITY)
        .build();

    doReturn(btcCoin).when(coinRepository).findOneBySymbol(eq(BTC));
    doReturn(usdCoin).when(coinRepository).findOneBySymbol(eq(USD));

    coinInitService.init();

    verify(coinRepository).findOneBySymbol(eq(BTC));
    verify(coinRepository).findOneBySymbol(eq(USD));
    verify(coinRepository, times(0)).saveAndFlush(eq(btcCoin));
    verify(coinRepository, times(0)).saveAndFlush(eq(usdCoin));
  }
}
