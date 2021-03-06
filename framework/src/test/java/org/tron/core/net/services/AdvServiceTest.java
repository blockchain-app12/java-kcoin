package org.kcoin.core.net.services;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kcoin.common.application.KcoinApplicationContext;
import org.kcoin.common.overlay.server.SyncPool;
import org.kcoin.common.parameter.CommonParameter;
import org.kcoin.common.utils.ReflectUtils;
import org.kcoin.common.utils.Sha256Hash;
import org.kcoin.core.Constant;
import org.kcoin.core.capsule.BlockCapsule;
import org.kcoin.core.config.DefaultConfig;
import org.kcoin.core.config.args.Args;
import org.kcoin.core.net.message.BlockMessage;
import org.kcoin.core.net.message.TransactionMessage;
import org.kcoin.core.net.peer.Item;
import org.kcoin.core.net.peer.PeerConnection;
import org.kcoin.core.net.service.AdvService;
import org.kcoin.protos.Protocol;
import org.kcoin.protos.Protocol.Inventory.InventoryType;

//@Ignore
public class AdvServiceTest {

  protected KcoinApplicationContext context;
  private AdvService service;
  private PeerConnection peer;
  private SyncPool syncPool;

  /**
   * init context.
   */
  @Before
  public void init() {
    Args.setParam(new String[]{"--output-directory", "output-directory", "--debug"},
        Constant.TEST_CONF);
    context = new KcoinApplicationContext(DefaultConfig.class);
    service = context.getBean(AdvService.class);
  }

  /**
   * destroy.
   */
  @After
  public void destroy() {
    Args.clearParam();
    context.destroy();
  }

  @Test
  public void test() {
    testAddInv();
    testBroadcast();
    testFastSend();
    testSymBroadcast();
  }

  private void testAddInv() {
    boolean flag;
    Item itemSym = new Item(Sha256Hash.ZERO_HASH, InventoryType.SYM);
    flag = service.addInv(itemSym);
    Assert.assertTrue(flag);
    flag = service.addInv(itemSym);
    Assert.assertFalse(flag);

    Item itemBlock = new Item(Sha256Hash.ZERO_HASH, InventoryType.BLOCK);
    flag = service.addInv(itemBlock);
    Assert.assertTrue(flag);
    flag = service.addInv(itemBlock);
    Assert.assertFalse(flag);

    service.addInvToCache(itemBlock);
    flag = service.addInv(itemBlock);
    Assert.assertFalse(flag);
  }

  private void testBroadcast() {

    try {
      peer = context.getBean(PeerConnection.class);
      syncPool = context.getBean(SyncPool.class);

      List<PeerConnection> peers = Lists.newArrayList();
      peers.add(peer);
      ReflectUtils.setFieldValue(syncPool, "activePeers", peers);
      BlockCapsule blockCapsule = new BlockCapsule(1, Sha256Hash.ZERO_HASH,
          System.currentTimeMillis(), Sha256Hash.ZERO_HASH.getByteString());
      BlockMessage msg = new BlockMessage(blockCapsule);
      service.broadcast(msg);
      Item item = new Item(blockCapsule.getBlockId(), InventoryType.BLOCK);
      Assert.assertNotNull(service.getMessage(item));

      peer.close();
      syncPool.close();
    } catch (NullPointerException e) {
      System.out.println(e);
    }
  }

  private void testFastSend() {

    try {
      peer = context.getBean(PeerConnection.class);
      syncPool = context.getBean(SyncPool.class);

      List<PeerConnection> peers = Lists.newArrayList();
      peers.add(peer);
      ReflectUtils.setFieldValue(syncPool, "activePeers", peers);
      BlockCapsule blockCapsule = new BlockCapsule(1, Sha256Hash.ZERO_HASH,
          System.currentTimeMillis(), Sha256Hash.ZERO_HASH.getByteString());
      BlockMessage msg = new BlockMessage(blockCapsule);
      service.fastForward(msg);
      Item item = new Item(blockCapsule.getBlockId(), InventoryType.BLOCK);
      //Assert.assertNull(service.getMessage(item));

      peer.getAdvInvRequest().put(item, System.currentTimeMillis());
      service.onDisconnect(peer);

      peer.close();
      syncPool.close();
    } catch (NullPointerException e) {
      System.out.println(e);
    }
  }

  private void testSymBroadcast() {
    Protocol.Transaction sym = Protocol.Transaction.newBuilder().build();
    CommonParameter.getInstance().setValidContractProtoThreadNum(1);
    TransactionMessage msg = new TransactionMessage(sym);
    service.broadcast(msg);
    Item item = new Item(msg.getMessageId(), InventoryType.SYM);
    Assert.assertNotNull(service.getMessage(item));
  }

}
