package com.botocrypt.aggregator.processor.cex;

import com.botocrypt.aggregator.processor.ExchangeProcessor;
import com.botocrypt.exchange.cex.io.api.TickersApi;
import com.botocrypt.exchange.cex.io.dto.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CexExchangeProcessor implements ExchangeProcessor {

  private final TickersApi tickersApi;

  @Autowired
  public CexExchangeProcessor(TickersApi tickersApi) {
    this.tickersApi = tickersApi;
  }

  @Override
  public void getCoinPrices() {
    final Mono<ApiResponseDto> monoResponse = tickersApi
        .getCryptocurrenciesPricesForOneMarketSymbol("BTC");
    if (monoResponse == null) {
      log.error("No data fetched from CEX.IO (monoResponse is null)");
      return;
    }

    final ApiResponseDto apiResponseDto = monoResponse.block();
    if (apiResponseDto == null) {
      log.error("No data fetched from CEX.IO (apiResponseDto is null)");
      return;
    }

    log.info("Response status: {}", apiResponseDto.getOk());
    log.debug("Response data: {}", apiResponseDto.getData());
  }
}
