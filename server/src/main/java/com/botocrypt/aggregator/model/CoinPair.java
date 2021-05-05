package com.botocrypt.aggregator.model;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
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
@IdClass(CoinPairIdentity.class)
public class CoinPair implements Serializable {

  @Id
  @Column(name = "first_coin_id")
  private Integer firstCoinId;

  @Id
  @Column(name = "second_coin_id")
  private Integer secondCoinId;

  @Id
  @Column(name = "exchange_id")
  private Integer exchangeId;

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
