package com.botocrypt.aggregator.repository;

import com.botocrypt.aggregator.model.CoinPair;
import com.botocrypt.aggregator.model.CoinPairIdentity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoinPairRepository extends JpaRepository<CoinPair, CoinPairIdentity> {

  List<CoinPair> findByCoinPairIdentityExchangeId(int exchangeId);
}
