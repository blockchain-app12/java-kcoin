package org.kcoin.core.services.ratelimiter.adapter;

import org.kcoin.core.services.ratelimiter.RuntimeData;

public interface IRateLimiter {

  boolean acquire(RuntimeData data);

}
