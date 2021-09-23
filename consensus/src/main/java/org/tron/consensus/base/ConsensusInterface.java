package org.kcoin.consensus.base;

import org.kcoin.core.capsule.BlockCapsule;

public interface ConsensusInterface {

  void start(Param param);

  void stop();

  void receiveBlock(BlockCapsule block);

  boolean validBlock(BlockCapsule block);

  boolean applyBlock(BlockCapsule block);

}