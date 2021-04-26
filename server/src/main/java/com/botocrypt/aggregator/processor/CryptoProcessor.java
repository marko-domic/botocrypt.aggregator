package com.botocrypt.aggregator.processor;

import java.util.List;
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
    exchangeProcessors.forEach(ExchangeProcessor::getCoinPrices);
    log.info("Fetching prices from exchanges finished.");
  }
}
