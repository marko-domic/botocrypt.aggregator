package com.botocrypt.aggregator.configuration;

import com.botocrypt.exchange.cex.io.api.TickersApi;
import com.botocrypt.exchange.cex.io.invoker.ApiClient;
import com.botocrypt.exchange.cex.io.invoker.RFC3339DateFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.DateFormat;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
@Import({
    TickersApi.class
})
public class ExchangeConfiguration {

  private final Jackson2ObjectMapperBuilder objectMapperBuilder;

  @Autowired
  public ExchangeConfiguration(
      Jackson2ObjectMapperBuilder objectMapperBuilder) {
    this.objectMapperBuilder = objectMapperBuilder;
  }

  @Bean
  public ApiClient cexApiClient() {

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

    final DateFormat dateFormat = new RFC3339DateFormat();
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    return new ApiClient(webClient, objectMapper, dateFormat);
  }
}