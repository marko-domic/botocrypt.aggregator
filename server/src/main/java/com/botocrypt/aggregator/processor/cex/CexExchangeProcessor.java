package com.botocrypt.aggregator.processor.cex;

import com.botocrypt.aggregator.model.CoinPair;
import com.botocrypt.aggregator.model.CryptoPairOrder;
import com.botocrypt.aggregator.model.Exchange;
import com.botocrypt.aggregator.processor.ExchangeProcessor;
import com.botocrypt.aggregator.repository.CoinPairRepository;
import com.botocrypt.aggregator.repository.ExchangeRepository;
import com.botocrypt.exchange.cex.io.api.OrderbookApi;
import com.botocrypt.exchange.cex.io.dto.OrderbookDto;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@Profile("service")
public class CexExchangeProcessor implements ExchangeProcessor {

  private static final String COIN_PAIR_SEPARATOR = ":";

  private final OrderbookApi orderbookApi;
  private final ExchangeRepository exchangeRepository;
  private final CoinPairRepository coinPairRepository;

  private final String CEX_EXCHANGE_NAME;

  @Autowired
  public CexExchangeProcessor(OrderbookApi orderbookApi, ExchangeRepository exchangeRepository,
      CoinPairRepository coinPairRepository,
      @Value("${aggregator.exchange.cex.name}") String cexExchangeName) {
    this.orderbookApi = orderbookApi;
    this.exchangeRepository = exchangeRepository;
    this.coinPairRepository = coinPairRepository;
    this.CEX_EXCHANGE_NAME = cexExchangeName;
  }

  @Override
  public List<CryptoPairOrder> getCoinPrices() {

    final Exchange exchange = exchangeRepository.findOneByName(CEX_EXCHANGE_NAME);
    if (exchange == null) {
      log.warn("{} exchange not found in repository.", CEX_EXCHANGE_NAME);
      return Collections.emptyList();
    }

    final List<CoinPair> coinPairs = coinPairRepository
        .findByCoinPairIdentityExchangeId(exchange.getId());
    if (CollectionUtils.isEmpty(coinPairs)) {
      log.warn("Coin pairs not found for {} exchange.", CEX_EXCHANGE_NAME);
      return Collections.emptyList();
    }

    return coinPairs
        .stream()
        .map(this::generateCryptoPairOrderFromExchange)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

  }

  private CryptoPairOrder generateCryptoPairOrderFromExchange(CoinPair coinPair) {

    if (StringUtils.isEmpty(coinPair.getMarketSymbol())) {
      log.warn("No market pair symbol set for coin pair with exchange {}, coins {} and {}.",
          coinPair.getExchange().getName(), coinPair.getFirstCoin().getSymbol(),
          coinPair.getSecondCoin().getSymbol());
      return null;
    }

    final String[] coinSymbols = coinPair.getMarketSymbol().split(COIN_PAIR_SEPARATOR);
    final Mono<OrderbookDto> monoResponse = orderbookApi
        .getOrderbookForCryptoPair(coinSymbols[0], coinSymbols[1]);

    if (monoResponse == null) {
      log.warn("No data fetched from CEX.IO (monoResponse is null)");
      return null;
    }

    final OrderbookDto orderbookDto = monoResponse.block();
    if (orderbookDto == null) {
      log.warn("No data fetched from CEX.IO (orderbookDto is null)");
      return null;
    }

    return convertOrderbookDtoToCryptoPairOrder(orderbookDto);
  }

  private CryptoPairOrder convertOrderbookDtoToCryptoPairOrder(OrderbookDto orderbookDto) {
    return null;
  }

}
