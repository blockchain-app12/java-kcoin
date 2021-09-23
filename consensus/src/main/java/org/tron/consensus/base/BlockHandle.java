package org.kcoin.consensus.base;

import org.kcoin.consensus.base.Param.Miner;
import org.kcoin.core.capsule.BlockCapsule;

public interface BlockHandle {

  State getState();

  Object getLock();

  BlockCapsule produce(Miner miner, long blockTime, long timeout);

}