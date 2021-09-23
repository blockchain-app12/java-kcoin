package org.kcoin.core.metrics;

import lombok.Data;
import org.kcoin.core.metrics.blockchain.BlockChainInfo;
import org.kcoin.core.metrics.net.NetInfo;
import org.kcoin.core.metrics.node.NodeInfo;

@Data
public class MetricsInfo {
  private long interval;
  private NodeInfo node;
  private BlockChainInfo blockchain;
  private NetInfo net;
}
