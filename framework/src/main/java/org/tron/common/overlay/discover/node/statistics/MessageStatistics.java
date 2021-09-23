package org.kcoin.common.overlay.discover.node.statistics;

import lombok.extern.slf4j.Slf4j;
import org.kcoin.common.net.udp.message.UdpMessageTypeEnum;
import org.kcoin.common.overlay.message.Message;
import org.kcoin.core.net.message.FetchInvDataMessage;
import org.kcoin.core.net.message.InventoryMessage;
import org.kcoin.core.net.message.MessageTypes;
import org.kcoin.core.net.message.TransactionsMessage;

@Slf4j
public class MessageStatistics {

  //udp discovery
  public final MessageCount discoverInPing = new MessageCount();
  public final MessageCount discoverOutPing = new MessageCount();
  public final MessageCount discoverInPong = new MessageCount();
  public final MessageCount discoverOutPong = new MessageCount();
  public final MessageCount discoverInFindNode = new MessageCount();
  public final MessageCount discoverOutFindNode = new MessageCount();
  public final MessageCount discoverInNeighbours = new MessageCount();
  public final MessageCount discoverOutNeighbours = new MessageCount();

  //tcp p2p
  public final MessageCount p2pInHello = new MessageCount();
  public final MessageCount p2pOutHello = new MessageCount();
  public final MessageCount p2pInPing = new MessageCount();
  public final MessageCount p2pOutPing = new MessageCount();
  public final MessageCount p2pInPong = new MessageCount();
  public final MessageCount p2pOutPong = new MessageCount();
  public final MessageCount p2pInDisconnect = new MessageCount();
  public final MessageCount p2pOutDisconnect = new MessageCount();

  //tcp kcoin
  public final MessageCount kcoinInMessage = new MessageCount();
  public final MessageCount kcoinOutMessage = new MessageCount();

  public final MessageCount kcoinInSyncBlockChain = new MessageCount();
  public final MessageCount kcoinOutSyncBlockChain = new MessageCount();
  public final MessageCount kcoinInBlockChainInventory = new MessageCount();
  public final MessageCount kcoinOutBlockChainInventory = new MessageCount();

  public final MessageCount kcoinInSymInventory = new MessageCount();
  public final MessageCount kcoinOutSymInventory = new MessageCount();
  public final MessageCount kcoinInSymInventoryElement = new MessageCount();
  public final MessageCount kcoinOutSymInventoryElement = new MessageCount();

  public final MessageCount kcoinInBlockInventory = new MessageCount();
  public final MessageCount kcoinOutBlockInventory = new MessageCount();
  public final MessageCount kcoinInBlockInventoryElement = new MessageCount();
  public final MessageCount kcoinOutBlockInventoryElement = new MessageCount();

  public final MessageCount kcoinInSymFetchInvData = new MessageCount();
  public final MessageCount kcoinOutSymFetchInvData = new MessageCount();
  public final MessageCount kcoinInSymFetchInvDataElement = new MessageCount();
  public final MessageCount kcoinOutSymFetchInvDataElement = new MessageCount();

  public final MessageCount kcoinInBlockFetchInvData = new MessageCount();
  public final MessageCount kcoinOutBlockFetchInvData = new MessageCount();
  public final MessageCount kcoinInBlockFetchInvDataElement = new MessageCount();
  public final MessageCount kcoinOutBlockFetchInvDataElement = new MessageCount();


  public final MessageCount kcoinInSym = new MessageCount();
  public final MessageCount kcoinOutSym = new MessageCount();
  public final MessageCount kcoinInSyms = new MessageCount();
  public final MessageCount kcoinOutSyms = new MessageCount();
  public final MessageCount kcoinInBlock = new MessageCount();
  public final MessageCount kcoinOutBlock = new MessageCount();
  public final MessageCount kcoinOutAdvBlock = new MessageCount();

  public void addUdpInMessage(UdpMessageTypeEnum type) {
    addUdpMessage(type, true);
  }

  public void addUdpOutMessage(UdpMessageTypeEnum type) {
    addUdpMessage(type, false);
  }

  public void addTcpInMessage(Message msg) {
    addTcpMessage(msg, true);
  }

  public void addTcpOutMessage(Message msg) {
    addTcpMessage(msg, false);
  }

  private void addUdpMessage(UdpMessageTypeEnum type, boolean flag) {
    switch (type) {
      case DISCOVER_PING:
        if (flag) {
          discoverInPing.add();
        } else {
          discoverOutPing.add();
        }
        break;
      case DISCOVER_PONG:
        if (flag) {
          discoverInPong.add();
        } else {
          discoverOutPong.add();
        }
        break;
      case DISCOVER_FIND_NODE:
        if (flag) {
          discoverInFindNode.add();
        } else {
          discoverOutFindNode.add();
        }
        break;
      case DISCOVER_NEIGHBORS:
        if (flag) {
          discoverInNeighbours.add();
        } else {
          discoverOutNeighbours.add();
        }
        break;
      default:
        break;
    }
  }

  private void addTcpMessage(Message msg, boolean flag) {

    if (flag) {
      kcoinInMessage.add();
    } else {
      kcoinOutMessage.add();
    }

    switch (msg.getType()) {
      case P2P_HELLO:
        if (flag) {
          p2pInHello.add();
        } else {
          p2pOutHello.add();
        }
        break;
      case P2P_PING:
        if (flag) {
          p2pInPing.add();
        } else {
          p2pOutPing.add();
        }
        break;
      case P2P_PONG:
        if (flag) {
          p2pInPong.add();
        } else {
          p2pOutPong.add();
        }
        break;
      case P2P_DISCONNECT:
        if (flag) {
          p2pInDisconnect.add();
        } else {
          p2pOutDisconnect.add();
        }
        break;
      case SYNC_BLOCK_CHAIN:
        if (flag) {
          kcoinInSyncBlockChain.add();
        } else {
          kcoinOutSyncBlockChain.add();
        }
        break;
      case BLOCK_CHAIN_INVENTORY:
        if (flag) {
          kcoinInBlockChainInventory.add();
        } else {
          kcoinOutBlockChainInventory.add();
        }
        break;
      case INVENTORY:
        InventoryMessage inventoryMessage = (InventoryMessage) msg;
        int inventorySize = inventoryMessage.getInventory().getIdsCount();
        messageProcess(inventoryMessage.getInvMessageType(),
                kcoinInSymInventory,kcoinInSymInventoryElement,kcoinInBlockInventory,
                kcoinInBlockInventoryElement,kcoinOutSymInventory,kcoinOutSymInventoryElement,
                kcoinOutBlockInventory,kcoinOutBlockInventoryElement,
                flag, inventorySize);
        break;
      case FETCH_INV_DATA:
        FetchInvDataMessage fetchInvDataMessage = (FetchInvDataMessage) msg;
        int fetchSize = fetchInvDataMessage.getInventory().getIdsCount();
        messageProcess(fetchInvDataMessage.getInvMessageType(),
                kcoinInSymFetchInvData,kcoinInSymFetchInvDataElement,kcoinInBlockFetchInvData,
                kcoinInBlockFetchInvDataElement,kcoinOutSymFetchInvData,kcoinOutSymFetchInvDataElement,
                kcoinOutBlockFetchInvData,kcoinOutBlockFetchInvDataElement,
                flag, fetchSize);
        break;
      case SYMS:
        TransactionsMessage transactionsMessage = (TransactionsMessage) msg;
        if (flag) {
          kcoinInSyms.add();
          kcoinInSym.add(transactionsMessage.getTransactions().getTransactionsCount());
        } else {
          kcoinOutSyms.add();
          kcoinOutSym.add(transactionsMessage.getTransactions().getTransactionsCount());
        }
        break;
      case SYM:
        if (flag) {
          kcoinInMessage.add();
        } else {
          kcoinOutMessage.add();
        }
        break;
      case BLOCK:
        if (flag) {
          kcoinInBlock.add();
        }
        kcoinOutBlock.add();
        break;
      default:
        break;
    }
  }
  
  
  private void messageProcess(MessageTypes messageType,
                              MessageCount inSym,
                              MessageCount inSymEle,
                              MessageCount inBlock,
                              MessageCount inBlockEle,
                              MessageCount outSym,
                              MessageCount outSymEle,
                              MessageCount outBlock,
                              MessageCount outBlockEle,
                              boolean flag, int size) {
    if (flag) {
      if (messageType == MessageTypes.SYM) {
        inSym.add();
        inSymEle.add(size);
      } else {
        inBlock.add();
        inBlockEle.add(size);
      }
    } else {
      if (messageType == MessageTypes.SYM) {
        outSym.add();
        outSymEle.add(size);
      } else {
        outBlock.add();
        outBlockEle.add(size);
      }
    }
  }

}
