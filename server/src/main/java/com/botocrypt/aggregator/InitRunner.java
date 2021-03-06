package com.botocrypt.aggregator;

import com.botocrypt.aggregator.processor.InitProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("init")
public class InitRunner implements ApplicationRunner {

  private final InitProcessor initProcessor;

  @Autowired
  public InitRunner(InitProcessor initProcessor) {
    this.initProcessor = initProcessor;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    initProcessor.initRepositoryWithNecessaryData();
  }
}
