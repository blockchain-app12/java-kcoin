package org.kcoin.core.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.kcoin.core.capsule.MarketAccountOrderCapsule;
import org.kcoin.core.db.KcoinStoreWithRevoking;
import org.kcoin.core.exception.ItemNotFoundException;

@Component
public class MarketAccountStore extends KcoinStoreWithRevoking<MarketAccountOrderCapsule> {

  @Autowired
  protected MarketAccountStore(@Value("market_account") String dbName) {
    super(dbName);
  }

  @Override
  public MarketAccountOrderCapsule get(byte[] key) throws ItemNotFoundException {
    byte[] value = revokingDB.get(key);
    return new MarketAccountOrderCapsule(value);
  }

}