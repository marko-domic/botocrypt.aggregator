package com.botocrypt.aggregator.processor;

import com.botocrypt.aggregator.model.CryptoPairOrder;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

public interface ExchangeProcessor {

  List<CryptoPairOrder> getCoinPrices();

  String exchangeName();

  default BigDecimal calculateAveragePrice(BigDecimal price, BigDecimal quantity) {
    if (quantity.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    return price.divide(quantity, MathContext.DECIMAL64);
  }

  @lombok.Value
  class CoinPriceQuantity {

    BigDecimal price;
    BigDecimal quantity;
  }
}
