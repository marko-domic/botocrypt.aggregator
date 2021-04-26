package com.botocrypt.aggregator.scheduler;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import com.botocrypt.aggregator.processor.CryptoProcessor;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component(SCOPE_PROTOTYPE)
@Profile("service")
public class TickersJob extends QuartzJobBean {

  private final CryptoProcessor cryptoProcessor;

  @Autowired
  public TickersJob(CryptoProcessor cryptoProcessor) {
    super();
    this.cryptoProcessor = cryptoProcessor;
  }

  @Override
  protected void executeInternal(JobExecutionContext context) {
    cryptoProcessor.processCryptoFromExchanges();
  }
}
