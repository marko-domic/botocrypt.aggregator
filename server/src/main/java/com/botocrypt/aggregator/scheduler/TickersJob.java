package com.botocrypt.aggregator.scheduler;

import com.botocrypt.aggregator.processor.ExchangeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
public class TickersJob extends QuartzJobBean {

  private final ExchangeProcessor exchangeProcessor;

  @Autowired
  public TickersJob(ExchangeProcessor exchangeProcessor) {
    this.exchangeProcessor = exchangeProcessor;
  }

  @Override
  protected void executeInternal(JobExecutionContext context) {
    log.info("Prices fetched from exchange.");
    exchangeProcessor.getCoinPrices();
  }
}
