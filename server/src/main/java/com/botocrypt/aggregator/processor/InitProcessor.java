package com.botocrypt.aggregator.processor;

import com.botocrypt.aggregator.service.InitService;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("init")
public class InitProcessor {

  private final List<InitService> initServices;

  @Autowired
  public InitProcessor(InitService coinInitService, InitService exchangeInitService,
      InitService coinPairInitService) {
    initServices = Arrays.asList(coinInitService, exchangeInitService, coinPairInitService);
  }

  public void initRepositoryWithNecessaryData() {
    log.info("Aggregator service initialization started.");
    initServices.forEach(InitService::init);
    log.info("Aggregator service initialization finished.");
  }
}
