package com.botocrypt.aggregator.processor;

import com.botocrypt.aggregator.model.CryptoPairOrder;
import com.botocrypt.arbitrage.api.Arbitrage.CoinPairInfo;
import com.botocrypt.arbitrage.api.Arbitrage.CoinPairOrder;
import com.botocrypt.arbitrage.api.ArbitrageServiceGrpc.ArbitrageServiceStub;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("service")
public class CryptoProcessor {

  private final List<ExchangeProcessor> exchangeProcessors;
  private final ObjectProvider<ArbitrageServiceStub> arbitrageServiceClientProvider;
  private final ObjectProvider<ArbitrageResponseObserver> arbitrageResponseObserverProvider;
  private final Supplier<String> cycleIdSupplier;

  @Autowired
  public CryptoProcessor(List<ExchangeProcessor> exchangeProcessors,
      ObjectProvider<ArbitrageServiceStub> arbitrageServiceClientProvider,
      ObjectProvider<ArbitrageResponseObserver> arbitrageResponseObserverProvider,
      Supplier<String> cycleIdSupplier) {
    this.exchangeProcessors = exchangeProcessors;
    this.arbitrageServiceClientProvider = arbitrageServiceClientProvider;
    this.arbitrageResponseObserverProvider = arbitrageResponseObserverProvider;
    this.cycleIdSupplier = cycleIdSupplier;
  }

  public void processCryptoFromExchanges() {

    log.info("Fetching prices from exchanges started.");

    final String cycleId = cycleIdSupplier.get();
    final ArbitrageServiceStub arbitrageServiceClient = arbitrageServiceClientProvider.getObject();
    final ArbitrageResponseObserver arbitrageResponseObserver = arbitrageResponseObserverProvider
        .getObject();
    final StreamObserver<CoinPairInfo> arbitrageRequestObserver = arbitrageServiceClient
        .sendCoinPairInfoFromExchange(arbitrageResponseObserver);

    exchangeProcessors.stream()
        .flatMap(exchangeProcessor -> exchangeProcessor.getCoinPrices().stream())
        .map(cryptoPairOrder -> convertCryptoPairOrderToCoinPairInfo(cryptoPairOrder, cycleId))
        .forEach(arbitrageRequestObserver::onNext);

    arbitrageRequestObserver.onCompleted();
    handleArbitrageResponse(arbitrageResponseObserver);

    log.info("Fetching prices from exchanges finished.");
  }

  private CoinPairInfo convertCryptoPairOrderToCoinPairInfo(CryptoPairOrder cryptoPairOrder,
      String cycleId) {
    return CoinPairInfo.newBuilder()
        .setCycleId(cycleId)
        .setExchange(cryptoPairOrder.getExchange())
        .setCoinPairOrder(createCoinPairOrder(cryptoPairOrder))
        .build();
  }

  private CoinPairOrder createCoinPairOrder(CryptoPairOrder cryptoPairOrder) {
    return CoinPairOrder.newBuilder()
        .setFirstCoin(cryptoPairOrder.getFirstCrypto())
        .setSecondCoin(cryptoPairOrder.getSecondCrypto())
        .setBidAveragePrice(cryptoPairOrder.getBidAveragePrice().doubleValue())
        .setBidQuantity(cryptoPairOrder.getBidQuantity().doubleValue())
        .setAskAveragePrice(cryptoPairOrder.getAskAveragePrice().doubleValue())
        .setAskQuantity(cryptoPairOrder.getAskQuantity().doubleValue())
        .setExchange(cryptoPairOrder.getExchange())
        .build();
  }

  private void handleArbitrageResponse(ArbitrageResponseObserver arbitrageResponseObserver) {
    try {
      arbitrageResponseObserver.waitForResponseToComplete();
    } catch (InterruptedException e) {
      log.error(e.getMessage(), e);
    }
  }
}
