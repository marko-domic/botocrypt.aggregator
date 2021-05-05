package com.botocrypt.aggregator.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoinPair {

  @EmbeddedId
  private CoinPairIdentity id;

  @MapsId("firstCoinId")
  @JoinColumn(name = "first_coin_id", referencedColumnName = "id")
  @ManyToOne
  private Coin firstCoin;

  @MapsId("secondCoinId")
  @JoinColumn(name = "second_coin_id", referencedColumnName = "id")
  @ManyToOne
  private Coin secondCoin;

  @MapsId("exchangeId")
  @JoinColumn(name = "exchange_id", referencedColumnName = "id")
  @ManyToOne
  private Exchange exchange;

  @NotNull
  @Column(name = "market_symbol")
  private String marketSymbol;
}
