package com.botocrypt.aggregator.service;

import com.botocrypt.aggregator.model.Exchange;
import com.botocrypt.aggregator.repository.ExchangeRepository;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Profile("init")
public class ExchangeInitService {

  private static final String[] EXCHANGES = {
      "CEX.IO"
  };

  private final ExchangeRepository exchangeRepository;

  @Autowired
  public ExchangeInitService(ExchangeRepository exchangeRepository) {
    this.exchangeRepository = exchangeRepository;
  }

  @Transactional
  public void init() {

    log.info("Initialization with exchanges started.");

    Arrays.stream(EXCHANGES).forEach(this::insertExchangeIfNotExists);

    log.info("Initialization with exchanges finished.");
  }

  private void insertExchangeIfNotExists(String exchangeName) {
    final Exchange existingExchange = exchangeRepository.findOneByName(exchangeName);
    if (existingExchange == null) {
      final Exchange exchange = new Exchange();
      exchange.setName(exchangeName);
      exchangeRepository.saveAndFlush(exchange);

      log.info("{} exchange saved in repository.", exchangeName);
      return;
    }

    log.debug("{} exchange already exists in repository.", exchangeName);
  }
}
