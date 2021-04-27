package com.botocrypt.aggregator.model;

import java.math.BigDecimal;
import lombok.Value;

@Value
public class CryptoPairOrder {

  String firstCrypto;
  String secondCrypto;
  BigDecimal bidAveragePrice;
  BigDecimal bidQuantity;
  BigDecimal askAveragePrice;
  BigDecimal askQuantity;
  String exchange;
}
