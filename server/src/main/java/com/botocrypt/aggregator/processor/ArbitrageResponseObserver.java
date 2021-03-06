package com.botocrypt.aggregator.processor;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import com.botocrypt.arbitrage.api.Arbitrage.CoinPairResponseDto;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope(SCOPE_PROTOTYPE)
@Profile("service")
public class ArbitrageResponseObserver implements StreamObserver<CoinPairResponseDto> {

  private final CountDownLatch finishLatch;

  public ArbitrageResponseObserver() {
    this.finishLatch = new CountDownLatch(1);
  }

  @Override
  public void onNext(CoinPairResponseDto value) {
    log.info("Coin pairs received in cycle: {} and with status: {}", value.getCycleId(),
        value.getStatus());
  }

  @Override
  public void onError(Throwable t) {
    // Only log specific exception and proceed with processing
    log.warn(
        "Something went wrong while receiving coin pairs in arbitrage service. Error message: {}",
        t.getMessage(), t);
  }

  @Override
  public void onCompleted() {
    log.info("Arbitrage response completed");
    finishLatch.countDown();
  }

  public void waitForResponseToComplete() throws InterruptedException {
    if (finishLatch.await(1, TimeUnit.MINUTES)) {
      log.info("Finished with receiving coin pairs");
      return;
    }
    log.error("Something went wrong. Response have not finished with its stream");
  }
}
