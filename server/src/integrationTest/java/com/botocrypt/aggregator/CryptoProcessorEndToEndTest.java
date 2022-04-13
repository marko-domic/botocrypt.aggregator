package com.botocrypt.aggregator;

import static com.botocrypt.arbitrage.api.ArbitrageServiceGrpc.getSendCoinPairInfoFromExchangeMethod;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.awaitility.Awaitility.await;
import static org.grpcmock.GrpcMock.clientStreamingMethod;
import static org.grpcmock.GrpcMock.grpcMock;
import static org.grpcmock.GrpcMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.botocrypt.aggregator.processor.InitProcessor;
import com.botocrypt.aggregator.repository.CoinPairRepository;
import com.botocrypt.aggregator.repository.CoinRepository;
import com.botocrypt.aggregator.repository.ExchangeRepository;
import com.botocrypt.arbitrage.api.Arbitrage.CoinPairDto;
import com.botocrypt.arbitrage.api.Arbitrage.CoinPairResponseDto;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.grpcmock.GrpcMock;
import org.grpcmock.springboot.AutoConfigureGrpcMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {
    "spring.h2.console.enabled=true"})
@Tag("integration")
@ActiveProfiles(value = {"service", "init"})
@TestPropertySource(properties = {
    "aggregator.scheduler.cron=0/5 * * ? * * *",
    "aggregator.exchange.cex.base-path= http://localhost:9099/api",
    "aggregator.exchange.binance.base-path= http://localhost:9098",
    "aggregator.arbitrage.service.host=localhost",
    "aggregator.arbitrage.service.port=9091"
})
@EnableTransactionManagement
@AutoConfigureGrpcMock
@ExtendWith(MockitoExtension.class)
public class CryptoProcessorEndToEndTest {

  private static final int GRPC_SERVICE_MOCK_PORT = 9091;

  private static WireMockServer cexMockServer;
  private static WireMockServer binanceMockServer;

  private static final CoinPairDtoHandler CoinPairDtoHandler = new CoinPairDtoHandler();

  @Autowired
  private InitProcessor initProcessor;

  @Autowired
  private CoinRepository coinRepository;

  @Autowired
  private ExchangeRepository exchangeRepository;

  @Autowired
  private CoinPairRepository coinPairRepository;

  @BeforeAll
  static void initMockServers() {

    GrpcMock.configureFor(grpcMock(GRPC_SERVICE_MOCK_PORT).build().start());
    stubFor(clientStreamingMethod(getSendCoinPairInfoFromExchangeMethod())
        .willProxyTo(responseObserver -> {
          CoinPairDtoHandler.setCoinPairDtoResponseObserver(responseObserver);
          return CoinPairDtoHandler;
        })
    );

    initCexMockServer();
    initBinanceMockServer();
  }

  @AfterAll
  public static void stopServices() {

    if (cexMockServer != null) {
      cexMockServer.stop();
      cexMockServer = null;
    }

    if (binanceMockServer != null) {
      binanceMockServer.stop();
      binanceMockServer = null;
    }
  }

  private static void initCexMockServer() {

    cexMockServer = new WireMockServer(
        options().port(9099).usingFilesUnderDirectory("src/integrationTest/resources")
            .notifier(new ConsoleNotifier(true)));
    cexMockServer.start();

    cexMockServer.addStubMapping(cexMockServer.stubFor(
        get(urlPathMatching("/api/order_book/BTC/USD")).atPriority(1).willReturn(
            aResponse().withStatus(200).withHeader("Content-Type", "text/json")
                .withBodyFile("orders/cex/order_1.json"))));

    cexMockServer.addStubMapping(cexMockServer.stubFor(
        get(urlPathMatching("/api/order_book/ETH/USD")).atPriority(1).willReturn(
            aResponse().withStatus(200).withHeader("Content-Type", "text/json")
                .withBodyFile("orders/cex/order_2.json"))));

    cexMockServer.addStubMapping(cexMockServer.stubFor(
        get(urlPathMatching("/api/order_book/ETH/BTC")).atPriority(1).willReturn(
            aResponse().withStatus(200).withHeader("Content-Type", "text/json")
                .withBodyFile("orders/cex/order_3.json"))));

    cexMockServer.addStubMapping(cexMockServer.stubFor(
        get(urlPathMatching("/api/order_book/XRP/USD")).atPriority(1).willReturn(
            aResponse().withStatus(200).withHeader("Content-Type", "text/json")
                .withBodyFile("orders/cex/order_4.json"))));

    cexMockServer.addStubMapping(cexMockServer.stubFor(
        get(urlPathMatching("/api/order_book/XRP/BTC")).atPriority(1).willReturn(
            aResponse().withStatus(200).withHeader("Content-Type", "text/json")
                .withBodyFile("orders/cex/order_5.json"))));

    cexMockServer.addStubMapping(cexMockServer.stubFor(
        get(urlPathMatching("/api/order_book/XLM/USD")).atPriority(1).willReturn(
            aResponse().withStatus(200).withHeader("Content-Type", "text/json")
                .withBodyFile("orders/cex/order_6.json"))));

    cexMockServer.addStubMapping(cexMockServer.stubFor(
        get(urlPathMatching("/api/order_book/XLM/BTC")).atPriority(1).willReturn(
            aResponse().withStatus(200).withHeader("Content-Type", "text/json")
                .withBodyFile("orders/cex/order_7.json"))));

    cexMockServer.addStubMapping(cexMockServer.stubFor(
        get(urlPathMatching("/api/order_book/LTC/USD")).atPriority(1).willReturn(
            aResponse().withStatus(200).withHeader("Content-Type", "text/json")
                .withBodyFile("orders/cex/order_8.json"))));

    cexMockServer.addStubMapping(cexMockServer.stubFor(
        get(urlPathMatching("/api/order_book/LTC/BTC")).atPriority(1).willReturn(
            aResponse().withStatus(200).withHeader("Content-Type", "text/json")
                .withBodyFile("orders/cex/order_9.json"))));

    cexMockServer.addStubMapping(cexMockServer.stubFor(
        get(urlPathMatching("/api/order_book/ADA/USD")).atPriority(1).willReturn(
            aResponse().withStatus(200).withHeader("Content-Type", "text/json")
                .withBodyFile("orders/cex/order_10.json"))));

    cexMockServer.addStubMapping(cexMockServer.stubFor(
        get(urlPathMatching("/api/order_book/BTC/USDT")).atPriority(1).willReturn(
            aResponse().withStatus(200).withHeader("Content-Type", "text/json")
                .withBodyFile("orders/cex/order_11.json"))));

    cexMockServer.addStubMapping(cexMockServer.stubFor(
        get(urlPathMatching("/api/order_book/ETH/USDT")).atPriority(1).willReturn(
            aResponse().withStatus(200).withHeader("Content-Type", "text/json")
                .withBodyFile("orders/cex/order_12.json"))));

    cexMockServer.addStubMapping(cexMockServer.stubFor(
        get(urlPathMatching("/api/order_book/XRP/USDT")).atPriority(1).willReturn(
            aResponse().withStatus(200).withHeader("Content-Type", "text/json")
                .withBodyFile("orders/cex/order_13.json"))));

    cexMockServer.addStubMapping(cexMockServer.stubFor(
        get(urlPathMatching("/api/order_book/XLM/USDT")).atPriority(1).willReturn(
            aResponse().withStatus(200).withHeader("Content-Type", "text/json")
                .withBodyFile("orders/cex/order_14.json"))));

    cexMockServer.addStubMapping(cexMockServer.stubFor(
        get(urlPathMatching("/api/order_book/LTC/USDT")).atPriority(1).willReturn(
            aResponse().withStatus(200).withHeader("Content-Type", "text/json")
                .withBodyFile("orders/cex/order_15.json"))));

    cexMockServer.addStubMapping(cexMockServer.stubFor(
        get(urlPathMatching("/api/order_book/ADA/USDT")).atPriority(1).willReturn(
            aResponse().withStatus(200).withHeader("Content-Type", "text/json")
                .withBodyFile("orders/cex/order_16.json"))));
  }

  private static void initBinanceMockServer() {

    binanceMockServer = new WireMockServer(
        options().port(9098).usingFilesUnderDirectory("src/integrationTest/resources")
            .notifier(new ConsoleNotifier(true)));
    binanceMockServer.start();

    binanceMockServer.addStubMapping(binanceMockServer.stubFor(
        get(urlPathMatching("/api/v3/depth"))
            .withQueryParam("symbol", equalTo("BTCUSDT"))
            .atPriority(1)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/json")
                .withBodyFile("orders/binance/order_1.json"))));

    binanceMockServer.addStubMapping(binanceMockServer.stubFor(
        get(urlPathMatching("/api/v3/depth"))
            .withQueryParam("symbol", equalTo("ETHUSDT"))
            .atPriority(1)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/json")
                .withBodyFile("orders/binance/order_2.json"))));

    binanceMockServer.addStubMapping(binanceMockServer.stubFor(
        get(urlPathMatching("/api/v3/depth"))
            .withQueryParam("symbol", equalTo("ETHBTC"))
            .atPriority(1)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/json")
                .withBodyFile("orders/binance/order_3.json"))));

    binanceMockServer.addStubMapping(binanceMockServer.stubFor(
        get(urlPathMatching("/api/v3/depth"))
            .withQueryParam("symbol", equalTo("XRPUSDT"))
            .atPriority(1)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/json")
                .withBodyFile("orders/binance/order_4.json"))));

    binanceMockServer.addStubMapping(binanceMockServer.stubFor(
        get(urlPathMatching("/api/v3/depth"))
            .withQueryParam("symbol", equalTo("XRPBTC"))
            .atPriority(1)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/json")
                .withBodyFile("orders/binance/order_5.json"))));

    binanceMockServer.addStubMapping(binanceMockServer.stubFor(
        get(urlPathMatching("/api/v3/depth"))
            .withQueryParam("symbol", equalTo("XRPETH"))
            .atPriority(1)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/json")
                .withBodyFile("orders/binance/order_6.json"))));

    binanceMockServer.addStubMapping(binanceMockServer.stubFor(
        get(urlPathMatching("/api/v3/depth"))
            .withQueryParam("symbol", equalTo("XLMBTC"))
            .atPriority(1)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/json")
                .withBodyFile("orders/binance/order_7.json"))));

    binanceMockServer.addStubMapping(binanceMockServer.stubFor(
        get(urlPathMatching("/api/v3/depth"))
            .withQueryParam("symbol", equalTo("XLMETH"))
            .atPriority(1)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/json")
                .withBodyFile("orders/binance/order_8.json"))));

    binanceMockServer.addStubMapping(binanceMockServer.stubFor(
        get(urlPathMatching("/api/v3/depth"))
            .withQueryParam("symbol", equalTo("XLMUSDT"))
            .atPriority(1)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/json")
                .withBodyFile("orders/binance/order_9.json"))));

    binanceMockServer.addStubMapping(binanceMockServer.stubFor(
        get(urlPathMatching("/api/v3/depth"))
            .withQueryParam("symbol", equalTo("LTCBTC"))
            .atPriority(1)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/json")
                .withBodyFile("orders/binance/order_10.json"))));

    binanceMockServer.addStubMapping(binanceMockServer.stubFor(
        get(urlPathMatching("/api/v3/depth"))
            .withQueryParam("symbol", equalTo("LTCETH"))
            .atPriority(1)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/json")
                .withBodyFile("orders/binance/order_11.json"))));

    binanceMockServer.addStubMapping(binanceMockServer.stubFor(
        get(urlPathMatching("/api/v3/depth"))
            .withQueryParam("symbol", equalTo("LTCUSDT"))
            .atPriority(1)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/json")
                .withBodyFile("orders/binance/order_12.json"))));

    binanceMockServer.addStubMapping(binanceMockServer.stubFor(
        get(urlPathMatching("/api/v3/depth"))
            .withQueryParam("symbol", equalTo("ADABTC"))
            .atPriority(1)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/json")
                .withBodyFile("orders/binance/order_13.json"))));

    binanceMockServer.addStubMapping(binanceMockServer.stubFor(
        get(urlPathMatching("/api/v3/depth"))
            .withQueryParam("symbol", equalTo("ADAETH"))
            .atPriority(1)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/json")
                .withBodyFile("orders/binance/order_14.json"))));

    binanceMockServer.addStubMapping(binanceMockServer.stubFor(
        get(urlPathMatching("/api/v3/depth"))
            .withQueryParam("symbol", equalTo("ADAUSDT"))
            .atPriority(1)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/json")
                .withBodyFile("orders/binance/order_15.json"))));
  }

  @Test
  void testFetchingOrdersFromExchangesAndSendThemToArbitrageService() {

    // Check if repository is initialized with necessary data
    initProcessor.initRepositoryWithNecessaryData();
    assertEquals(8, coinRepository.count());
    assertEquals(2, exchangeRepository.count());
    assertEquals(31, coinPairRepository.count());

    await().atMost(6, TimeUnit.SECONDS).until(CoinPairDtoHandler::grpcServerResponded);

    final List<CoinPairDto> coinPairs = CoinPairDtoHandler.getCoinPairs();
    assertEquals(31, coinPairs.size());

    // Validate number of coin pairs from CEX.IO exchange
    final long numOfCoinsOnCex = coinPairs.stream()
        .filter(coinPairDto -> coinPairDto.getExchange().equals("CEX.IO"))
        .count();
    assertEquals(16L, numOfCoinsOnCex);

    // Validate number of coin pairs from Binance exchange
    final long numOfCoinsOnBinance = coinPairs.stream()
        .filter(coinPairDto -> coinPairDto.getExchange().equals("Binance"))
        .count();
    assertEquals(15L, numOfCoinsOnBinance);
  }

  private static class CoinPairDtoHandler implements StreamObserver<CoinPairDto> {

    @Getter
    private final List<CoinPairDto> coinPairs = new ArrayList<>();

    private final CountDownLatch finishLatch = new CountDownLatch(1);

    @Setter
    private StreamObserver<CoinPairResponseDto> CoinPairDtoResponseObserver;

    @Override
    public void onNext(CoinPairDto value) {
      coinPairs.add(value);
    }

    @Override
    public void onError(Throwable t) {
      log.warn("Error while receiving request stream. Proceed with processing. Error message: {}",
          t.getMessage(), t);
    }

    @Override
    public void onCompleted() {
      final CoinPairResponseDto CoinPairDtoResponse = CoinPairResponseDto.newBuilder()
          .setCycleId(coinPairs.isEmpty() ? null : coinPairs.get(0).getCycleId())
          .setStatus("SUCCESS")
          .build();
      CoinPairDtoResponseObserver.onNext(CoinPairDtoResponse);
      CoinPairDtoResponseObserver.onCompleted();
      finishLatch.countDown();
    }

    public boolean grpcServerResponded() throws InterruptedException {
      return finishLatch.await(5, TimeUnit.SECONDS);
    }
  }
}
