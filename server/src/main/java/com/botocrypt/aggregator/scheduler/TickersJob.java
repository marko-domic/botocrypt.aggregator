package com.botocrypt.aggregator.scheduler;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import com.botocrypt.aggregator.processor.ExchangeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component(SCOPE_PROTOTYPE)
@Profile("service")
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
