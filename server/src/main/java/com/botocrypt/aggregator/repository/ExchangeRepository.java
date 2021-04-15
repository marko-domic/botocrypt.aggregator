package com.botocrypt.aggregator.repository;

import com.botocrypt.aggregator.model.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRepository extends JpaRepository<Exchange, Integer> {

  Exchange findOneByName(String name);
}
