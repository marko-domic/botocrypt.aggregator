package com.botocrypt.aggregator.processor;

import com.botocrypt.aggregator.service.CoinInitService;
import com.botocrypt.aggregator.service.CoinPairInitService;
import com.botocrypt.aggregator.service.ExchangeInitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("init")
public class InitProcessor {

  private final CoinInitService coinInitService;
  private final ExchangeInitService exchangeInitService;
  private final CoinPairInitService coinPairInitService;

  @Autowired
  public InitProcessor(CoinInitService coinInitService, ExchangeInitService exchangeInitService,
      CoinPairInitService coinPairInitService) {
    this.coinInitService = coinInitService;
    this.exchangeInitService = exchangeInitService;
    this.coinPairInitService = coinPairInitService;
  }

  public void initRepositoryWithNecessaryData() {
    coinInitService.init();
    exchangeInitService.init();
    coinPairInitService.init();
  }
}
