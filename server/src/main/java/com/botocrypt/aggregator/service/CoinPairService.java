package com.botocrypt.aggregator.service;

import com.botocrypt.aggregator.model.CoinPair;
import com.botocrypt.aggregator.model.Exchange;
import com.botocrypt.aggregator.repository.CoinPairRepository;
import com.botocrypt.aggregator.repository.ExchangeRepository;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Profile("service")
public class CoinPairService {

  private final ExchangeRepository exchangeRepository;
  private final CoinPairRepository coinPairRepository;

  @Autowired
  public CoinPairService(ExchangeRepository exchangeRepository,
      CoinPairRepository coinPairRepository) {
    this.exchangeRepository = exchangeRepository;
    this.coinPairRepository = coinPairRepository;
  }

  @Transactional
  public List<CoinPair> getCoinPairsFromRepository(@NotNull String exchangeName) {

    final Exchange exchange = exchangeRepository.findOneByName(exchangeName);
    if (exchange == null) {
      log.warn("{} exchange not found in repository.", exchangeName);
      return Collections.emptyList();
    }

    final List<CoinPair> coinPairs = coinPairRepository.findByIdExchangeId(exchange.getId());
    if (CollectionUtils.isEmpty(coinPairs)) {
      log.warn("Coin pairs not found for {} exchange.", exchangeName);
      return Collections.emptyList();
    }

    return coinPairs;
  }
}
