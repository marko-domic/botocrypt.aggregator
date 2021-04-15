package com.botocrypt.aggregator.configuration;


import com.botocrypt.aggregator.scheduler.TickersJob;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@Profile("service")
public class TickersQuartzScheduler {

  @Value("${aggregator.scheduler.cron}")
  private String cronExpression;

  @Bean
  public Trigger tickersTrigger(JobDetail tickersJobDetail) {
    return TriggerBuilder.newTrigger()
        .forJob(tickersJobDetail)
        .withIdentity(tickersJobDetail.getKey().getName(), "tickers-triggers")
        .withDescription("Fetch Coin Prices Trigger")
        .startAt(Date.from(Instant.now()))
        .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
        .build();
  }

  @Bean
  public JobDetail tickersJobDetail() {
    return JobBuilder.newJob(TickersJob.class)
        .withIdentity(UUID.randomUUID().toString(), "tickers-jobs")
        .withDescription("Fetch Coin Prices Job")
        .storeDurably()
        .build();
  }
}
