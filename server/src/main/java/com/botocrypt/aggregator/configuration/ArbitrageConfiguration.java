package com.botocrypt.aggregator.configuration;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import com.botocrypt.arbitrage.api.ArbitrageServiceGrpc;
import com.botocrypt.arbitrage.api.ArbitrageServiceGrpc.ArbitrageServiceStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

@Configuration
@Profile("service")
public class ArbitrageConfiguration {

  private final String ARBITRAGE_SERVICE_HOST;
  private final int ARBITRAGE_SERVICE_PORT;

  @Autowired
  public ArbitrageConfiguration(
      @Value("${aggregator.arbitrage.service.host}") String arbitrageServiceHost,
      @Value("${aggregator.arbitrage.service.port}") Integer arbitragePort) {
    ARBITRAGE_SERVICE_HOST = arbitrageServiceHost;
    ARBITRAGE_SERVICE_PORT = arbitragePort;
  }

  @Bean
  public ManagedChannel arbitrageGrpcChannel() {
    return ManagedChannelBuilder.forAddress(ARBITRAGE_SERVICE_HOST, ARBITRAGE_SERVICE_PORT)
        .usePlaintext()
        .build();
  }

  @Bean
  @Scope(SCOPE_PROTOTYPE)
  public ArbitrageServiceStub arbitrageServiceClient(ManagedChannel arbitrageGrpcChannel) {
     return ArbitrageServiceGrpc.newStub(arbitrageGrpcChannel);
  }

  @Bean
  public Supplier<String> cycleIdSupplier() {
    return () -> UUID.randomUUID().toString();
  }
}
