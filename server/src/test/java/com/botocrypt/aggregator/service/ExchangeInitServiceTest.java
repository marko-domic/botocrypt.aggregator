package com.botocrypt.aggregator.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.botocrypt.aggregator.model.Exchange;
import com.botocrypt.aggregator.repository.ExchangeRepository;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExchangeInitServiceTest {

  private static final String[] EXCHANGES = {
      "CEX.IO"
  };

  @Mock
  private ExchangeRepository exchangeRepository;

  @InjectMocks
  private ExchangeInitService exchangeInitService;

  @Test
  void testInit() {

    Arrays.stream(EXCHANGES).forEach(exchangeName -> {
      final Exchange exchange = Exchange.builder().name(exchangeName).build();
      doReturn(null).when(exchangeRepository).findOneByName(eq(exchangeName));
      doReturn(exchange).when(exchangeRepository).saveAndFlush(eq(exchange));
    });

    exchangeInitService.init();

    Arrays.stream(EXCHANGES).forEach(exchangeName -> {
      final Exchange exchange = Exchange.builder().name(exchangeName).build();
      verify(exchangeRepository).findOneByName(eq(exchangeName));
      verify(exchangeRepository).saveAndFlush(eq(exchange));
    });
  }

  @Test
  void testInitIfExchangesAreInRepository() {

    Arrays.stream(EXCHANGES).forEach(exchangeName -> {
      final Exchange exchange = Exchange.builder().name(exchangeName).build();
      doReturn(exchange).when(exchangeRepository).findOneByName(eq(exchangeName));
    });

    exchangeInitService.init();

    Arrays.stream(EXCHANGES).forEach(exchangeName -> {
      final Exchange exchange = Exchange.builder().name(exchangeName).build();
      verify(exchangeRepository).findOneByName(eq(exchangeName));
    });

    verify(exchangeRepository, times(0)).saveAndFlush(any(Exchange.class));
  }
}
