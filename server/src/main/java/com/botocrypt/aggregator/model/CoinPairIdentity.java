package com.botocrypt.aggregator.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CoinPairIdentity implements Serializable {

  @NotNull
  @EqualsAndHashCode.Include
  @Column(name = "first_coin_id")
  private int firstCoinId;

  @NotNull
  @EqualsAndHashCode.Include
  @Column(name = "second_coin_id")
  private int secondCoinId;

  @NotNull
  @EqualsAndHashCode.Include
  @Column(name = "exchange_id")
  private int exchangeId;

}
