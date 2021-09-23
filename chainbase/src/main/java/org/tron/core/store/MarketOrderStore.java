package org.kcoin.core.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.kcoin.core.capsule.MarketOrderCapsule;
import org.kcoin.core.db.KcoinStoreWithRevoking;
import org.kcoin.core.exception.ItemNotFoundException;

@Component
public class MarketOrderStore extends KcoinStoreWithRevoking<MarketOrderCapsule> {

  @Autowired
  protected MarketOrderStore(@Value("market_order") String dbName) {
    super(dbName);
  }

  @Override
  public MarketOrderCapsule get(byte[] key) throws ItemNotFoundException {
    byte[] value = revokingDB.get(key);
    return new MarketOrderCapsule(value);
  }

}