package com.botocrypt.aggregator.model;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CoinPair implements Serializable {

  @EmbeddedId
  private CoinPairIdentity coinPairIdentity;

  @OneToOne(cascade = CascadeType.ALL)
  @PrimaryKeyJoinColumn(name = "first_coin_id")
  private Coin firstCoin;

  @OneToOne(cascade = CascadeType.ALL)
  @PrimaryKeyJoinColumn(name = "second_coin_id")
  private Coin secondCoin;

  @OneToOne(cascade = CascadeType.ALL)
  @PrimaryKeyJoinColumn(name = "exchange_id")
  private Exchange exchange;

  @NotNull
  @Column(name = "market_symbol")
  private String marketSymbol;
}
