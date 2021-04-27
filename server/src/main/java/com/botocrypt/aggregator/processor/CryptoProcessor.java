package com.botocrypt.aggregator.processor;

import com.botocrypt.aggregator.model.CryptoPairOrder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("service")
public class CryptoProcessor {

  private final List<ExchangeProcessor> exchangeProcessors;

  @Autowired
  public CryptoProcessor(List<ExchangeProcessor> exchangeProcessors) {
    this.exchangeProcessors = exchangeProcessors;
  }

  public void processCryptoFromExchanges() {
    log.info("Fetching prices from exchanges started.");
    Map<String, List<CryptoPairOrder>> exchangePairOrders = exchangeProcessors.stream().collect(
        Collectors.toMap(ExchangeProcessor::exchangeName, ExchangeProcessor::getCoinPrices));
    // TODO: Implement logic for sending exchangePairOrders to another service for processing.
    log.info("Fetching prices from exchanges finished.");
  }
}
