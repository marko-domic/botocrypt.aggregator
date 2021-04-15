package com.botocrypt.aggregator.model;

import java.math.BigDecimal;
import lombok.Value;

@Value
public class CryptoPairOrder {

  String firstCrypto;
  String secondCrypto;
  BigDecimal bidPrice;
  BigDecimal bidQuantity;
  BigDecimal askPrice;
  BigDecimal askQuantity;
}
