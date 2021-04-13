package com.botocrypt.aggregator.processor.cex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.botocrypt.exchange.cex.io.api.TickersApi;
import com.botocrypt.exchange.cex.io.dto.ApiResponseDto;
import com.botocrypt.exchange.cex.io.dto.TickerDto;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class CexExchangeProcessorTest {

  private static final String FIRST_MARKET_SYMBOL = "BTC";

  @Mock
  private TickersApi tickersApi;

  @InjectMocks
  private CexExchangeProcessor cexExchangeProcessor;

  @Test
  void testGetCoinPrices() {
    final TickerDto tickerDto = new TickerDto();
    tickerDto.setTimestamp("1618296889");
    tickerDto.setPair("ETH:BTC");
    tickerDto.setLow("0.035309");
    tickerDto.setHigh("0.036036");
    tickerDto.setLast("0.035711");
    tickerDto.setVolume("592.35001800");
    tickerDto.setVolume30d("5320.38276800");
    tickerDto.setBid(0.035711);
    tickerDto.setAsk(0.035719);

    final ApiResponseDto apiResponseDto = new ApiResponseDto();
    apiResponseDto.setOk("ok");
    apiResponseDto.setE("tickers");
    apiResponseDto.setData(Collections.singletonList(tickerDto));

    final Mono<ApiResponseDto> monoResponse = Mono.just(apiResponseDto);

    doReturn(monoResponse).when(tickersApi)
        .getCryptocurrenciesPricesForOneMarketSymbol(eq(FIRST_MARKET_SYMBOL));

    cexExchangeProcessor.getCoinPrices();

    verify(tickersApi).getCryptocurrenciesPricesForOneMarketSymbol(eq(FIRST_MARKET_SYMBOL));
  }

  @Test
  void testGetCoinPricesWithException() {

    final String exceptionMessage = "Exception on getCoinPrices method call";

    doThrow(new RuntimeException(exceptionMessage)).when(tickersApi)
        .getCryptocurrenciesPricesForOneMarketSymbol(eq(FIRST_MARKET_SYMBOL));

    final Exception exception = assertThrows(RuntimeException.class,
        () -> cexExchangeProcessor.getCoinPrices());

    assertEquals(exceptionMessage, exception.getMessage());
    verify(tickersApi).getCryptocurrenciesPricesForOneMarketSymbol(eq(FIRST_MARKET_SYMBOL));
  }
}
