package com.botocrypt.aggregator.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CoinPairIdentity implements Serializable {

  @NotNull
  @EqualsAndHashCode.Include
  @Column(name = "first_coin_id")
  private Integer firstCoinId;

  @NotNull
  @EqualsAndHashCode.Include
  @Column(name = "second_coin_id")
  private Integer secondCoinId;

  @NotNull
  @EqualsAndHashCode.Include
  @Column(name = "exchange_id")
  private Integer exchangeId;

}
