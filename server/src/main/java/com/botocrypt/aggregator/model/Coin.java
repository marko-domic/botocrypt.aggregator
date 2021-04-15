package com.botocrypt.aggregator.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Coin {

  @Id
  @NotNull
  @EqualsAndHashCode.Include
  private int id;

  @NotBlank
  private String symbol;

  private double minAmount;
}
