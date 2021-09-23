package org.kcoin.consensus.base;

import org.kcoin.consensus.pbft.message.PbftBaseMessage;
import org.kcoin.core.capsule.BlockCapsule;

public interface PbftInterface {

  boolean isSyncing();

  void forwardMessage(PbftBaseMessage message);

  BlockCapsule getBlock(long blockNum) throws Exception;

}