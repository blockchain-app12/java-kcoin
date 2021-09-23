package org.kcoin.core.consensus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.kcoin.common.backup.BackupManager;
import org.kcoin.common.backup.BackupManager.BackupStatusEnum;
import org.kcoin.consensus.Consensus;
import org.kcoin.consensus.base.BlockHandle;
import org.kcoin.consensus.base.Param.Miner;
import org.kcoin.consensus.base.State;
import org.kcoin.core.capsule.BlockCapsule;
import org.kcoin.core.db.Manager;
import org.kcoin.core.net.KcoinNetService;
import org.kcoin.core.net.message.BlockMessage;

@Slf4j(topic = "consensus")
@Component
public class BlockHandleImpl implements BlockHandle {

  @Autowired
  private Manager manager;

  @Autowired
  private BackupManager backupManager;

  @Autowired
  private KcoinNetService kcoinNetService;

  @Autowired
  private Consensus consensus;

  @Override
  public State getState() {
    if (!backupManager.getStatus().equals(BackupStatusEnum.MASTER)) {
      return State.BACKUP_IS_NOT_MASTER;
    }
    return State.OK;
  }

  public Object getLock() {
    return manager;
  }

  public BlockCapsule produce(Miner miner, long blockTime, long timeout) {
    BlockCapsule blockCapsule = manager.generateBlock(miner, blockTime, timeout);
    if (blockCapsule == null) {
      return null;
    }
    try {
      consensus.receiveBlock(blockCapsule);
      BlockMessage blockMessage = new BlockMessage(blockCapsule);
      kcoinNetService.fastForward(blockMessage);
      manager.pushBlock(blockCapsule);
      kcoinNetService.broadcast(blockMessage);
    } catch (Exception e) {
      logger.error("Handle block {} failed.", blockCapsule.getBlockId().getString(), e);
      return null;
    }
    return blockCapsule;
  }
}
