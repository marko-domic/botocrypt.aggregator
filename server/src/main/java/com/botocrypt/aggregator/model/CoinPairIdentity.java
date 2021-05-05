package com.botocrypt.aggregator.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CoinPairIdentity implements Serializable {

  @NotNull
  @Column(name = "first_coin_id")
  private Integer firstCoinId;

  @NotNull
  @Column(name = "second_coin_id")
  private Integer secondCoinId;

  @NotNull
  @Column(name = "exchange_id")
  private Integer exchangeId;

}
