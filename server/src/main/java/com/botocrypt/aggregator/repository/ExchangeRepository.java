package com.botocrypt.aggregator.repository;

import com.botocrypt.aggregator.model.Coin;
import com.botocrypt.aggregator.model.Exchange;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRepository extends JpaRepository<Exchange, Integer> {

  Exchange findOneByName(String name);

  List<Exchange> findByNameIn(Collection<String> names);
}
