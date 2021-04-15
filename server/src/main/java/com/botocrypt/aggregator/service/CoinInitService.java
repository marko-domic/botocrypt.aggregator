package com.botocrypt.aggregator.service;

import com.botocrypt.aggregator.model.Coin;
import com.botocrypt.aggregator.repository.CoinRepository;
import java.util.Arrays;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Profile("init")
public class CoinInitService {

  private static final CoinInfo[] COINS = {
      new CoinInfo("BTC", 0.15944),
      new CoinInfo("USD", 10000)
  };

  private final CoinRepository coinRepository;

  @Autowired
  public CoinInitService(CoinRepository coinRepository) {
    this.coinRepository = coinRepository;
  }

  @Transactional
  public void init() {

    log.info("Initialization with cryptocurrencies started.");

    Arrays.stream(COINS).forEach(this::insertCoinIfNotExists);

    log.info("Initialization with cryptocurrencies finished.");
  }

  private void insertCoinIfNotExists(CoinInfo coinInfo) {
    final Coin existingCoin = coinRepository.findOneBySymbol(coinInfo.getSymbol());
    if (existingCoin == null) {
      final Coin coin = new Coin();
      coin.setSymbol(coinInfo.getSymbol());
      coin.setMinAmount(coinInfo.getQuantity());
      coinRepository.saveAndFlush(coin);

      log.info("{} coin saved in repository.", coinInfo.getSymbol());
      return;
    }

    log.debug("{} coin already exists in repository.", coinInfo.getSymbol());
  }

  @Value
  private static class CoinInfo {

    String symbol;
    double quantity;
  }
}
