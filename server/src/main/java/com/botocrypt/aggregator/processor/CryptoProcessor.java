package com.botocrypt.aggregator.processor;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CryptoProcessor {

  private final List<ExchangeProcessor> exchangeProcessors;

  @Autowired
  public CryptoProcessor(List<ExchangeProcessor> exchangeProcessors) {
    this.exchangeProcessors = exchangeProcessors;
  }

  public void processCryptoFromExchanges() {
    exchangeProcessors.forEach(ExchangeProcessor::getCoinPrices);
  }
}
