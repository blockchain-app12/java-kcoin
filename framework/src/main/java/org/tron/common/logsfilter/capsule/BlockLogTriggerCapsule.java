package org.kcoin.common.logsfilter.capsule;

import lombok.Getter;
import lombok.Setter;
import org.kcoin.common.logsfilter.EventPluginLoader;
import org.kcoin.common.logsfilter.trigger.BlockLogTrigger;
import org.kcoin.core.capsule.BlockCapsule;

public class BlockLogTriggerCapsule extends TriggerCapsule {

  @Getter
  @Setter
  private BlockLogTrigger blockLogTrigger;

  public BlockLogTriggerCapsule(BlockCapsule block) {
    blockLogTrigger = new BlockLogTrigger();
    blockLogTrigger.setBlockHash(block.getBlockId().toString());
    blockLogTrigger.setTimeStamp(block.getTimeStamp());
    blockLogTrigger.setBlockNumber(block.getNum());
    blockLogTrigger.setTransactionSize(block.getTransactions().size());
    block.getTransactions().forEach(sym ->
        blockLogTrigger.getTransactionList().add(sym.getTransactionId().toString())
    );
  }

  public void setLatestSolidifiedBlockNumber(long latestSolidifiedBlockNumber) {
    blockLogTrigger.setLatestSolidifiedBlockNumber(latestSolidifiedBlockNumber);
  }

  @Override
  public void processTrigger() {
    EventPluginLoader.getInstance().postBlockTrigger(blockLogTrigger);
  }
}
