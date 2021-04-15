package com.botocrypt.aggregator.processor;

import com.botocrypt.aggregator.model.CryptoPairOrder;
import java.util.List;

public interface ExchangeProcessor {

  List<CryptoPairOrder> getCoinPrices();

}
