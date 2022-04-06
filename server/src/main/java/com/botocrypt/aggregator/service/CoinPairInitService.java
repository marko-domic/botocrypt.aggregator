package com.botocrypt.aggregator.service;

import com.botocrypt.aggregator.model.Coin;
import com.botocrypt.aggregator.model.CoinPair;
import com.botocrypt.aggregator.model.CoinPairIdentity;
import com.botocrypt.aggregator.model.Exchange;
import com.botocrypt.aggregator.repository.CoinPairRepository;
import com.botocrypt.aggregator.repository.CoinRepository;
import com.botocrypt.aggregator.repository.ExchangeRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Profile("init")
public class CoinPairInitService implements InitService {

  private static final String[] COINS = {
      "BTC",
      "USD",
      "ETH",
      "XRP",
      "XLM",
      "LTC",
      "ADA"
  };

  private static final String[] EXCHANGES = {
      "CEX.IO"
  };

  private static final CoinPairInfo[] COIN_PAIR_INFOS = {
      new CoinPairInfo("BTC", "USD", "CEX.IO", "BTC:USD"),
      new CoinPairInfo("ETH", "USD", "CEX.IO", "ETH:USD"),
      new CoinPairInfo("ETH", "BTC", "CEX.IO", "ETH:BTC"),
      new CoinPairInfo("XRP", "USD", "CEX.IO", "XRP:USD"),
      new CoinPairInfo("XRP", "BTC", "CEX.IO", "XRP:BTC"),
      new CoinPairInfo("XLM", "USD", "CEX.IO", "XLM:USD"),
      new CoinPairInfo("XLM", "BTC", "CEX.IO", "XLM:BTC"),
      new CoinPairInfo("LTC", "USD", "CEX.IO", "LTC:USD"),
      new CoinPairInfo("LTC", "BTC", "CEX.IO", "LTC:BTC"),
      new CoinPairInfo("ADA", "USD", "CEX.IO", "ADA:USD")
  };

  private final CoinRepository coinRepository;
  private final ExchangeRepository exchangeRepository;
  private final CoinPairRepository coinPairRepository;

  @Autowired
  public CoinPairInitService(CoinRepository coinRepository, ExchangeRepository exchangeRepository,
      CoinPairRepository coinPairRepository) {
    this.coinRepository = coinRepository;
    this.exchangeRepository = exchangeRepository;
    this.coinPairRepository = coinPairRepository;
  }

  @Override
  @Transactional
  public void init() {

    log.info("Initialization with coin pairs started.");

    final Map<String, Coin> coinsMap = getCoinsMap();
    final Map<String, Exchange> exchangesMap = getExchangesMap();

    Arrays.stream(COIN_PAIR_INFOS)
        .forEach(coinPairInfo -> insertCoinPairIfNotExists(coinPairInfo, coinsMap, exchangesMap));

    log.info("Initialization with coin pairs finished.");
  }

  private Map<String, Coin> getCoinsMap() {
    final List<Coin> coins = coinRepository.findBySymbolIn(Arrays.asList(COINS));
    return coins.stream().collect(Collectors.toMap(Coin::getSymbol, coin -> coin));
  }

  private Map<String, Exchange> getExchangesMap() {
    final List<Exchange> exchanges = exchangeRepository.findByNameIn(Arrays.asList(EXCHANGES));
    return exchanges.stream().collect(Collectors.toMap(Exchange::getName, exchange -> exchange));
  }

  private void insertCoinPairIfNotExists(CoinPairInfo coinPairInfo, Map<String, Coin> coinsMap,
      Map<String, Exchange> exchangesMap) {
    final Coin firstCoin = coinsMap.get(coinPairInfo.getFirstCoin());
    final Coin secondCoin = coinsMap.get(coinPairInfo.getSecondCoin());
    final Exchange exchange = exchangesMap.get(coinPairInfo.getExchange());
    final CoinPairIdentity coinPairIdentity = new CoinPairIdentity(firstCoin.getId(),
        secondCoin.getId(), exchange.getId());
    final Optional<CoinPair> coinPairOptional = coinPairRepository.findById(coinPairIdentity);
    if (coinPairOptional.isEmpty()) {
      final CoinPair coinPair = new CoinPair();
      coinPair.setId(coinPairIdentity);
      coinPair.setFirstCoin(firstCoin);
      coinPair.setSecondCoin(secondCoin);
      coinPair.setExchange(exchange);
      coinPair.setMarketSymbol(coinPairInfo.getMarketPairSymbol());
      coinPairRepository.saveAndFlush(coinPair);

      log.info("{} coin pair saved in repository.", coinPairInfo.getMarketPairSymbol());
      return;
    }

    log.debug("{} coin pair already exists in repository.", coinPairInfo.getMarketPairSymbol());
  }

  @Value
  private static class CoinPairInfo {

    String firstCoin;
    String secondCoin;
    String exchange;
    String marketPairSymbol;
  }
}
