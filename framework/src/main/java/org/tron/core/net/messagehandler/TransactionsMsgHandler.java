package org.kcoin.core.net.messagehandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.kcoin.core.config.args.Args;
import org.kcoin.core.exception.P2pException;
import org.kcoin.core.exception.P2pException.TypeEnum;
import org.kcoin.core.net.KcoinNetDelegate;
import org.kcoin.core.net.message.TransactionMessage;
import org.kcoin.core.net.message.TransactionsMessage;
import org.kcoin.core.net.message.KcoinMessage;
import org.kcoin.core.net.peer.Item;
import org.kcoin.core.net.peer.PeerConnection;
import org.kcoin.core.net.service.AdvService;
import org.kcoin.protos.Protocol.Inventory.InventoryType;
import org.kcoin.protos.Protocol.ReasonCode;
import org.kcoin.protos.Protocol.Transaction;
import org.kcoin.protos.Protocol.Transaction.Contract.ContractType;

@Slf4j(topic = "net")
@Component
public class TransactionsMsgHandler implements KcoinMsgHandler {

  private static int MAX_SYM_SIZE = 50_000;
  private static int MAX_SMART_CONTRACT_SUBMIT_SIZE = 100;
  @Autowired
  private KcoinNetDelegate kcoinNetDelegate;
  @Autowired
  private AdvService advService;

  private BlockingQueue<SymEvent> smartContractQueue = new LinkedBlockingQueue(MAX_SYM_SIZE);

  private BlockingQueue<Runnable> queue = new LinkedBlockingQueue();

  private int threadNum = Args.getInstance().getValidateSignThreadNum();
  private ExecutorService symHandlePool = new ThreadPoolExecutor(threadNum, threadNum, 0L,
      TimeUnit.MILLISECONDS, queue);

  private ScheduledExecutorService smartContractExecutor = Executors
      .newSingleThreadScheduledExecutor();

  public void init() {
    handleSmartContract();
  }

  public void close() {
    smartContractExecutor.shutdown();
  }

  public boolean isBusy() {
    return queue.size() + smartContractQueue.size() > MAX_SYM_SIZE;
  }

  @Override
  public void processMessage(PeerConnection peer, KcoinMessage msg) throws P2pException {
    TransactionsMessage transactionsMessage = (TransactionsMessage) msg;
    check(peer, transactionsMessage);
    for (Transaction sym : transactionsMessage.getTransactions().getTransactionsList()) {
      int type = sym.getRawData().getContract(0).getType().getNumber();
      if (type == ContractType.TriggerSmartContract_VALUE
          || type == ContractType.CreateSmartContract_VALUE) {
        if (!smartContractQueue.offer(new SymEvent(peer, new TransactionMessage(sym)))) {
          logger.warn("Add smart contract failed, queueSize {}:{}", smartContractQueue.size(),
              queue.size());
        }
      } else {
        symHandlePool.submit(() -> handleTransaction(peer, new TransactionMessage(sym)));
      }
    }
  }

  private void check(PeerConnection peer, TransactionsMessage msg) throws P2pException {
    for (Transaction sym : msg.getTransactions().getTransactionsList()) {
      Item item = new Item(new TransactionMessage(sym).getMessageId(), InventoryType.SYM);
      if (!peer.getAdvInvRequest().containsKey(item)) {
        throw new P2pException(TypeEnum.BAD_MESSAGE,
            "sym: " + msg.getMessageId() + " without request.");
      }
      peer.getAdvInvRequest().remove(item);
    }
  }

  private void handleSmartContract() {
    smartContractExecutor.scheduleWithFixedDelay(() -> {
      try {
        while (queue.size() < MAX_SMART_CONTRACT_SUBMIT_SIZE) {
          SymEvent event = smartContractQueue.take();
          symHandlePool.submit(() -> handleTransaction(event.getPeer(), event.getMsg()));
        }
      } catch (Exception e) {
        logger.error("Handle smart contract exception.", e);
      }
    }, 1000, 20, TimeUnit.MILLISECONDS);
  }

  private void handleTransaction(PeerConnection peer, TransactionMessage sym) {
    if (peer.isDisconnect()) {
      logger.warn("Drop sym {} from {}, peer is disconnect.", sym.getMessageId(),
          peer.getInetAddress());
      return;
    }

    if (advService.getMessage(new Item(sym.getMessageId(), InventoryType.SYM)) != null) {
      return;
    }

    try {
      kcoinNetDelegate.pushTransaction(sym.getTransactionCapsule());
      advService.broadcast(sym);
    } catch (P2pException e) {
      logger.warn("Sym {} from peer {} process failed. type: {}, reason: {}",
          sym.getMessageId(), peer.getInetAddress(), e.getType(), e.getMessage());
      if (e.getType().equals(TypeEnum.BAD_SYM)) {
        peer.disconnect(ReasonCode.BAD_TX);
      }
    } catch (Exception e) {
      logger.error("Sym {} from peer {} process failed.", sym.getMessageId(), peer.getInetAddress(),
          e);
    }
  }

  class SymEvent {

    @Getter
    private PeerConnection peer;
    @Getter
    private TransactionMessage msg;
    @Getter
    private long time;

    public SymEvent(PeerConnection peer, TransactionMessage msg) {
      this.peer = peer;
      this.msg = msg;
      this.time = System.currentTimeMillis();
    }
  }
}