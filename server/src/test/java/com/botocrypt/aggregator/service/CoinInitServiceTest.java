package com.botocrypt.aggregator.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
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
  private static final String ETH = "ETH";
  private static final double BTC_QUANTITY = 0.15944;
  private static final double USD_QUANTITY = 10000;
  private static final double ETH_QUANTITY = 3;

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
    final Coin ethCoin = Coin.builder()
        .symbol(ETH)
        .minAmount(ETH_QUANTITY)
        .build();

    doReturn(null).when(coinRepository).findOneBySymbol(eq(BTC));
    doReturn(null).when(coinRepository).findOneBySymbol(eq(USD));
    doReturn(null).when(coinRepository).findOneBySymbol(eq(ETH));
    doReturn(btcCoin).when(coinRepository).saveAndFlush(eq(btcCoin));
    doReturn(usdCoin).when(coinRepository).saveAndFlush(eq(usdCoin));
    doReturn(ethCoin).when(coinRepository).saveAndFlush(eq(ethCoin));

    coinInitService.init();

    verify(coinRepository).findOneBySymbol(eq(BTC));
    verify(coinRepository).findOneBySymbol(eq(USD));
    verify(coinRepository).findOneBySymbol(eq(ETH));
    verify(coinRepository).saveAndFlush(eq(btcCoin));
    verify(coinRepository).saveAndFlush(eq(usdCoin));
    verify(coinRepository).saveAndFlush(eq(ethCoin));
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
    final Coin ethCoin = Coin.builder()
        .symbol(ETH)
        .minAmount(ETH_QUANTITY)
        .build();

    doReturn(btcCoin).when(coinRepository).findOneBySymbol(eq(BTC));
    doReturn(usdCoin).when(coinRepository).findOneBySymbol(eq(USD));
    doReturn(ethCoin).when(coinRepository).findOneBySymbol(eq(ETH));

    coinInitService.init();

    verify(coinRepository).findOneBySymbol(eq(BTC));
    verify(coinRepository).findOneBySymbol(eq(USD));
    verify(coinRepository).findOneBySymbol(eq(ETH));
    verify(coinRepository, never()).saveAndFlush(eq(btcCoin));
    verify(coinRepository, never()).saveAndFlush(eq(usdCoin));
    verify(coinRepository, never()).saveAndFlush(eq(ethCoin));
  }
}
