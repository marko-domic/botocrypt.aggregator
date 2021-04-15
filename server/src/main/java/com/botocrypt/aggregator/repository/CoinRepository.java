package com.botocrypt.aggregator.repository;

import com.botocrypt.aggregator.model.Coin;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoinRepository extends JpaRepository<Coin, Integer> {

  Coin findOneBySymbol(String symbol);

  List<Coin> findBySymbolIn(Collection<String> symbols);
}
