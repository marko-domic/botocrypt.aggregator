package com.botocrypt.aggregator.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.DateFormat;
import java.util.TimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Import({
    com.botocrypt.exchange.cex.io.api.TickersApi.class,
    com.botocrypt.exchange.cex.io.api.OrderBookApi.class,
    com.botocrypt.exchange.binance.api.OrderBookApi.class
})
@Profile("service")
public class ExchangeConfiguration {

  private final Jackson2ObjectMapperBuilder objectMapperBuilder;

  @Value("${aggregator.exchange.cex.base-path}")
  private String cexBasePath;

  @Value("${aggregator.exchange.binance.base-path}")
  private String binanceBasePath;

  @Autowired
  public ExchangeConfiguration(
      Jackson2ObjectMapperBuilder objectMapperBuilder) {
    this.objectMapperBuilder = objectMapperBuilder;
  }

  @Bean
  @Profile("service")
  public com.botocrypt.exchange.cex.io.invoker.ApiClient cexApiClient() {

    final ObjectMapper objectMapper = objectMapperBuilder.build();

    final ExchangeStrategies strategies = ExchangeStrategies
        .builder()
        .codecs(clientDefaultCodecsConfigurer -> {
          clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonEncoder(
              new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
          clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonDecoder(
              new Jackson2JsonDecoder(objectMapper, new MediaType("text", "json")));
        }).build();
    final WebClient webClient = WebClient.builder().exchangeStrategies(strategies).build();

    final DateFormat dateFormat = new com.botocrypt.exchange.cex.io.invoker.RFC3339DateFormat();
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    return new com.botocrypt.exchange.cex.io.invoker.ApiClient(webClient, objectMapper, dateFormat)
        .setBasePath(cexBasePath);
  }

  @Bean
  @Profile("service")
  public com.botocrypt.exchange.binance.invoker.ApiClient binanceApiClient() {

    final ObjectMapper objectMapper = objectMapperBuilder.build();

    final ExchangeStrategies strategies = ExchangeStrategies
        .builder()
        .codecs(clientDefaultCodecsConfigurer -> {
          clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonEncoder(
              new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
          clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonDecoder(
              new Jackson2JsonDecoder(objectMapper, new MediaType("text", "json")));
        }).build();
    final WebClient webClient = WebClient.builder().exchangeStrategies(strategies).build();

    final DateFormat dateFormat = new com.botocrypt.exchange.binance.invoker.RFC3339DateFormat();
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    return new com.botocrypt.exchange.binance.invoker.ApiClient(webClient, objectMapper, dateFormat)
        .setBasePath(binanceBasePath);
  }
}
