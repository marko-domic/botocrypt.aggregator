CREATE TABLE IF NOT EXISTS coin
(
  id INT NOT NULL AUTO_INCREMENT,
  symbol VARCHAR(10) NOT NULL,
  min_amount DOUBLE,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS exchange
(
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(24) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS coin_pair
(
  first_coin_id INT NOT NULL,
  second_coin_id INT NOT NULL,
  exchange_id INT NOT NULL,
  market_symbol VARCHAR(24) NOT NULL,
  FOREIGN KEY (first_coin_id) REFERENCES coin(id) ON DELETE CASCADE,
  FOREIGN KEY (second_coin_id) REFERENCES coin(id) ON DELETE CASCADE,
  FOREIGN KEY (exchange_id) REFERENCES exchange(id) ON DELETE CASCADE,
  PRIMARY KEY (first_coin_id, second_coin_id, exchange_id)
);

