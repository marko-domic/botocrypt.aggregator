package com.botocrypt.aggregator.processor.binance;

import com.botocrypt.aggregator.model.CoinPair;
import com.botocrypt.aggregator.model.CryptoPairOrder;
import com.botocrypt.aggregator.processor.ExchangeProcessor;
import com.botocrypt.aggregator.service.CoinPairService;
import com.botocrypt.exchange.binance.api.OrderBookApi;
import com.botocrypt.exchange.binance.dto.OrderBookDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@Profile("service")
public class BinanceExchangeProcessor implements ExchangeProcessor {

  private final OrderBookApi orderBookApi;
  private final CoinPairService coinPairService;

  private final String BINANCE_EXCHANGE_NAME;

  @Autowired
  public BinanceExchangeProcessor(OrderBookApi orderBookApi, CoinPairService coinPairService,
      @Value("${aggregator.exchange.binance.name}") String binanceExchangeName) {
    this.orderBookApi = orderBookApi;
    this.coinPairService = coinPairService;
    BINANCE_EXCHANGE_NAME = binanceExchangeName;
  }

  @Override
  public List<CryptoPairOrder> getCoinPrices() {

    final List<CoinPair> coinPairs = coinPairService.getCoinPairsFromRepository(
        BINANCE_EXCHANGE_NAME);
    return coinPairs
        .stream()
        .map(this::generateCryptoPairOrderFromExchange)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public String exchangeName() {
    return BINANCE_EXCHANGE_NAME;
  }

  private CryptoPairOrder generateCryptoPairOrderFromExchange(CoinPair coinPair) {

    if (StringUtils.isEmpty(coinPair.getMarketSymbol())) {
      log.warn("No market pair symbol set for coin pair with exchange {}, coins {} and {}.",
          coinPair.getExchange().getName(), coinPair.getFirstCoin().getSymbol(),
          coinPair.getSecondCoin().getSymbol());
      return null;
    }

    final String coinPairSymbol = coinPair.getMarketSymbol();
    final Mono<OrderBookDto> monoResponse = orderBookApi.getOrderBookForCryptoPair(coinPairSymbol);

    if (monoResponse == null) {
      log.warn("No data fetched from Binance (monoResponse is null)");
      return null;
    }

    final OrderBookDto orderBookDto = monoResponse.block();
    if (orderBookDto == null) {
      log.warn("No data fetched from Binance (orderBookDto is null)");
      return null;
    }

    return convertOrderBookDtoToCryptoPairOrder(orderBookDto, coinPair);
  }

  private CryptoPairOrder convertOrderBookDtoToCryptoPairOrder(OrderBookDto orderBookDto,
      CoinPair coinPair) {
    final String firstCoinSymbol = coinPair.getFirstCoin().getSymbol();
    final String secondCoinSymbol = coinPair.getSecondCoin().getSymbol();
    final CoinPriceQuantity bidPriceQuantity = generateCoinPriceQuantity(orderBookDto.getBids(),
        coinPair.getFirstCoin().getMinAmount());
    final CoinPriceQuantity askPriceQuantity = generateCoinPriceQuantity(orderBookDto.getAsks(),
        coinPair.getFirstCoin().getMinAmount());
    return new CryptoPairOrder(
        firstCoinSymbol,
        secondCoinSymbol,
        calculateAveragePrice(bidPriceQuantity.getPrice(), bidPriceQuantity.getQuantity()),
        bidPriceQuantity.getQuantity(),
        calculateAveragePrice(askPriceQuantity.getPrice(), askPriceQuantity.getQuantity()),
        askPriceQuantity.getQuantity(),
        BINANCE_EXCHANGE_NAME);
  }

  private CoinPriceQuantity generateCoinPriceQuantity(List<List<BigDecimal>> orders,
      double minQuantity) {

    if (orders == null) {
      log.warn("List of orders is not defined (it is null)");
      return new CoinPriceQuantity(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    BigDecimal price = BigDecimal.ZERO;
    BigDecimal quantity = BigDecimal.ZERO;
    final BigDecimal marginQuantity = BigDecimal.valueOf(minQuantity);

    for (List<BigDecimal> order : orders) {
      price = price.add(order.get(0).multiply(order.get(1)));
      quantity = quantity.add(order.get(1));
      if (quantity.compareTo(marginQuantity) > 0) {
        break;
      }
    }

    return new CoinPriceQuantity(price, quantity);
  }
}
