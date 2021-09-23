package org.kcoin.core.services.ratelimiter.adapter;

import org.kcoin.core.services.ratelimiter.RuntimeData;
import org.kcoin.core.services.ratelimiter.strategy.QpsStrategy;

public class QpsRateLimiterAdapter implements IRateLimiter {

  private QpsStrategy strategy;

  public QpsRateLimiterAdapter(String paramString) {
    strategy = new QpsStrategy(paramString);
  }

  @Override
  public boolean acquire(RuntimeData data) {
    return strategy.acquire();
  }

}