package com.botocrypt.aggregator;

import static com.botocrypt.arbitrage.api.ArbitrageServiceGrpc.getSendCoinPairInfoFromExchangeMethod;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.awaitility.Awaitility.await;
import static org.grpcmock.GrpcMock.clientStreamingMethod;
import static org.grpcmock.GrpcMock.grpcMock;
import static org.grpcmock.GrpcMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.botocrypt.aggregator.processor.InitProcessor;
import com.botocrypt.aggregator.repository.CoinPairRepository;
import com.botocrypt.aggregator.repository.CoinRepository;
import com.botocrypt.aggregator.repository.ExchangeRepository;
import com.botocrypt.arbitrage.api.Arbitrage.CoinPairDto;
import com.botocrypt.arbitrage.api.Arbitrage.CoinPairOrderDto;
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
    "aggregator.arbitrage.service.host=localhost",
    "aggregator.arbitrage.service.port=9091"
})
@EnableTransactionManagement
@AutoConfigureGrpcMock
@ExtendWith(MockitoExtension.class)
public class CryptoProcessorEndToEndTest {

  private static final int GRPC_SERVICE_MOCK_PORT = 9091;

  private static WireMockServer cexMockServer;

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
  }

  @AfterAll
  public static void stopServices() {
    if (cexMockServer != null) {
      cexMockServer.stop();
      cexMockServer = null;
    }
  }

  @Test
  void testFetchingOrdersFromExchangesAndSendThemToArbitrageService() {

    // Check if repository is initialized with necessary data
    initProcessor.initRepositoryWithNecessaryData();
    assertEquals(7, coinRepository.count());
    assertEquals(1, exchangeRepository.count());
    assertEquals(10, coinPairRepository.count());

    await().atMost(6, TimeUnit.SECONDS).until(CoinPairDtoHandler::grpcServerResponded);

    final List<CoinPairDto> coinPairs = CoinPairDtoHandler.getCoinPairs();
    assertEquals(10, coinPairs.size());

    final CoinPairDto firstCoinPairDto = coinPairs.get(0);
    assertEquals("CEX.IO", firstCoinPairDto.getExchange());

    final CoinPairOrderDto coinPairOrder = firstCoinPairDto.getCoinPairOrder();
    assertNotNull(coinPairOrder);
    assertEquals("BTC", coinPairOrder.getFirstCoin());
    assertEquals("USD", coinPairOrder.getSecondCoin());
    assertEquals(57476.6, coinPairOrder.getBidAveragePrice());
    assertEquals(0.22709, coinPairOrder.getBidQuantity());
    assertEquals(57516.42004921007, coinPairOrder.getAskAveragePrice());
    assertEquals(0.64864775, coinPairOrder.getAskQuantity());
    assertEquals("CEX.IO", coinPairOrder.getExchange());

    final CoinPairDto secondCoinPairDto = coinPairs.get(1);
    assertEquals("CEX.IO", secondCoinPairDto.getExchange());

    final CoinPairOrderDto secondCoinPairOrder = secondCoinPairDto.getCoinPairOrder();
    assertNotNull(secondCoinPairOrder);
    assertEquals("ETH", secondCoinPairOrder.getFirstCoin());
    assertEquals("USD", secondCoinPairOrder.getSecondCoin());
    assertEquals(3952.680320793128, secondCoinPairOrder.getBidAveragePrice());
    assertEquals(5.056031, secondCoinPairOrder.getBidQuantity());
    assertEquals(3958.409991182018, secondCoinPairOrder.getAskAveragePrice());
    assertEquals(3.719672, secondCoinPairOrder.getAskQuantity());
    assertEquals("CEX.IO", secondCoinPairOrder.getExchange());

    final CoinPairDto thirdCoinPairDto = coinPairs.get(2);
    assertEquals("CEX.IO", thirdCoinPairDto.getExchange());

    final CoinPairOrderDto thirdCoinPairOrder = thirdCoinPairDto.getCoinPairOrder();
    assertNotNull(thirdCoinPairOrder);
    assertEquals("ETH", thirdCoinPairOrder.getFirstCoin());
    assertEquals("BTC", thirdCoinPairOrder.getSecondCoin());
    assertEquals(0.07796275676751092, thirdCoinPairOrder.getBidAveragePrice());
    assertEquals(6.373392, thirdCoinPairOrder.getBidQuantity());
    assertEquals(0.07806816867106742, thirdCoinPairOrder.getAskAveragePrice());
    assertEquals(6.383454, thirdCoinPairOrder.getAskQuantity());
    assertEquals("CEX.IO", thirdCoinPairOrder.getExchange());

    final CoinPairDto fourthCoinPairDto = coinPairs.get(3);
    assertEquals("CEX.IO", fourthCoinPairDto.getExchange());

    final CoinPairOrderDto fourthCoinPairOrder = fourthCoinPairDto.getCoinPairOrder();
    assertNotNull(fourthCoinPairOrder);
    assertEquals("XRP", fourthCoinPairOrder.getFirstCoin());
    assertEquals("USD", fourthCoinPairOrder.getSecondCoin());
    assertEquals(0.8165450184387861, fourthCoinPairOrder.getBidAveragePrice());
    assertEquals(27249.364902, fourthCoinPairOrder.getBidQuantity());
    assertEquals(0.8178960420490105, fourthCoinPairOrder.getAskAveragePrice());
    assertEquals(12995.2309, fourthCoinPairOrder.getAskQuantity());
    assertEquals("CEX.IO", fourthCoinPairOrder.getExchange());

    final CoinPairDto fifthCoinPairDto = coinPairs.get(4);
    assertEquals("CEX.IO", fifthCoinPairDto.getExchange());

    final CoinPairOrderDto fifthCoinPairOrder = fifthCoinPairDto.getCoinPairOrder();
    assertNotNull(fifthCoinPairOrder);
    assertEquals("XRP", fifthCoinPairOrder.getFirstCoin());
    assertEquals("BTC", fifthCoinPairOrder.getSecondCoin());
    assertEquals(0.00001790548703485728, fifthCoinPairOrder.getBidAveragePrice());
    assertEquals(6170.875072, fifthCoinPairOrder.getBidQuantity());
    assertEquals(0.00001802122143318497, fifthCoinPairOrder.getAskAveragePrice());
    assertEquals(12546.215543, fifthCoinPairOrder.getAskQuantity());
    assertEquals("CEX.IO", fifthCoinPairOrder.getExchange());

    final CoinPairDto sixthCoinPairDto = coinPairs.get(5);
    assertEquals("CEX.IO", sixthCoinPairDto.getExchange());

    final CoinPairOrderDto sixthCoinPairOrder = sixthCoinPairDto.getCoinPairOrder();
    assertNotNull(sixthCoinPairOrder);
    assertEquals("XLM", sixthCoinPairOrder.getFirstCoin());
    assertEquals("USD", sixthCoinPairOrder.getSecondCoin());
    assertEquals(0.2223011125679784, sixthCoinPairOrder.getBidAveragePrice());
    assertEquals(19188.9498693, sixthCoinPairOrder.getBidQuantity());
    assertEquals(0.2226642421292595, sixthCoinPairOrder.getAskAveragePrice());
    assertEquals(14416.0135872, sixthCoinPairOrder.getAskQuantity());
    assertEquals("CEX.IO", sixthCoinPairOrder.getExchange());

    final CoinPairDto seventhCoinPairDto = coinPairs.get(6);
    assertEquals("CEX.IO", seventhCoinPairDto.getExchange());

    final CoinPairOrderDto seventhCoinPairOrder = seventhCoinPairDto.getCoinPairOrder();
    assertNotNull(seventhCoinPairOrder);
    assertEquals("XLM", seventhCoinPairOrder.getFirstCoin());
    assertEquals("BTC", seventhCoinPairOrder.getSecondCoin());
    assertEquals(0.000004788942618267644, seventhCoinPairOrder.getBidAveragePrice());
    assertEquals(8710.8266808, seventhCoinPairOrder.getBidQuantity());
    assertEquals(0.000004917748285017264, seventhCoinPairOrder.getAskAveragePrice());
    assertEquals(7610.3816244, seventhCoinPairOrder.getAskQuantity());
    assertEquals("CEX.IO", seventhCoinPairOrder.getExchange());

    final CoinPairDto eighthCoinPairDto = coinPairs.get(7);
    assertEquals("CEX.IO", eighthCoinPairDto.getExchange());

    final CoinPairOrderDto eighthCoinPairOrder = eighthCoinPairDto.getCoinPairOrder();
    assertNotNull(eighthCoinPairOrder);
    assertEquals("LTC", eighthCoinPairOrder.getFirstCoin());
    assertEquals("USD", eighthCoinPairOrder.getSecondCoin());
    assertEquals(120.9233583974112, eighthCoinPairOrder.getBidAveragePrice());
    assertEquals(38.43538842, eighthCoinPairOrder.getBidQuantity());
    assertEquals(121.1021115883859, eighthCoinPairOrder.getAskAveragePrice());
    assertEquals(22.37896561, eighthCoinPairOrder.getAskQuantity());
    assertEquals("CEX.IO", eighthCoinPairOrder.getExchange());

    final CoinPairDto ninthCoinPairDto = coinPairs.get(8);
    assertEquals("CEX.IO", ninthCoinPairDto.getExchange());

    final CoinPairOrderDto ninthCoinPairOrder = ninthCoinPairDto.getCoinPairOrder();
    assertNotNull(ninthCoinPairOrder);
    assertEquals("LTC", ninthCoinPairOrder.getFirstCoin());
    assertEquals("BTC", ninthCoinPairOrder.getSecondCoin());
    assertEquals(0.002652206043218867, ninthCoinPairOrder.getBidAveragePrice());
    assertEquals(213.39690379, ninthCoinPairOrder.getBidQuantity());
    assertEquals(0.002660395975875171, ninthCoinPairOrder.getAskAveragePrice());
    assertEquals(133.62031036, ninthCoinPairOrder.getAskQuantity());
    assertEquals("CEX.IO", ninthCoinPairOrder.getExchange());

    final CoinPairDto tenthCoinPairDto = coinPairs.get(9);
    assertEquals("CEX.IO", tenthCoinPairDto.getExchange());

    final CoinPairOrderDto tenthCoinPairOrder = tenthCoinPairDto.getCoinPairOrder();
    assertNotNull(tenthCoinPairOrder);
    assertEquals("ADA", tenthCoinPairOrder.getFirstCoin());
    assertEquals("USD", tenthCoinPairOrder.getSecondCoin());
    assertEquals(1.15617978015954, tenthCoinPairOrder.getBidAveragePrice());
    assertEquals(11831.964069, tenthCoinPairOrder.getBidQuantity());
    assertEquals(1.157906028368539, tenthCoinPairOrder.getAskAveragePrice());
    assertEquals(12886.129137, tenthCoinPairOrder.getAskQuantity());
    assertEquals("CEX.IO", tenthCoinPairOrder.getExchange());
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
