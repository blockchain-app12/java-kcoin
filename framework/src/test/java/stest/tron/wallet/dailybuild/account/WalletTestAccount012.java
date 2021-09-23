package stest.kcoin.wallet.dailybuild.account;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.kcoin.api.GrpcAPI.AccountResourceMessage;
import org.kcoin.api.WalletGrpc;
import org.kcoin.common.crypto.ECKey;
import org.kcoin.common.utils.ByteArray;
import org.kcoin.common.utils.Utils;
import org.kcoin.core.Wallet;
import org.kcoin.protos.Protocol.Account;
import stest.kcoin.wallet.common.client.Configuration;
import stest.kcoin.wallet.common.client.Parameter.CommonConstant;
import stest.kcoin.wallet.common.client.utils.PublicMethed;

@Slf4j
public class WalletTestAccount012 {
  private static final long sendAmount = 10000000000L;
  private static final long frozenAmountForKcoinPower = 3456789L;
  private static final long frozenAmountForNet = 7000000L;
  private final String foundationKey = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final byte[] foundationAddress = PublicMethed.getFinalAddress(foundationKey);

  private final String witnessKey = Configuration.getByPath("testng.conf")
      .getString("witness.key1");
  private final byte[] witnessAddress = PublicMethed.getFinalAddress(witnessKey);

  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] frozenAddress = ecKey1.getAddress();
  String frozenKey = ByteArray.toHexString(ecKey1.getPrivKeyBytes());

  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list")
      .get(0);

  /**
   * constructor.
   */
  @BeforeClass(enabled = true)
  public void beforeClass() {
    PublicMethed.printAddress(frozenKey);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);

  }

  @Test(enabled = true, description = "Freeze balance to get kcoin power")
  public void test01FreezeBalanceGetKcoinPower() {


    final Long beforeFrozenTime = System.currentTimeMillis();
    Assert.assertTrue(PublicMethed.sendcoin(frozenAddress, sendAmount,
        foundationAddress, foundationKey, blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);


    AccountResourceMessage accountResource = PublicMethed
        .getAccountResource(frozenAddress, blockingStubFull);
    final Long beforeTotalKcoinPowerWeight = accountResource.getTotalKcoinPowerWeight();
    final Long beforeKcoinPowerLimit = accountResource.getKcoinPowerLimit();


    Assert.assertTrue(PublicMethed.freezeBalanceGetKcoinPower(frozenAddress,frozenAmountForKcoinPower,
        0,2,null,frozenKey,blockingStubFull));
    Assert.assertTrue(PublicMethed.freezeBalanceGetKcoinPower(frozenAddress,frozenAmountForNet,
        0,0,null,frozenKey,blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Long afterFrozenTime = System.currentTimeMillis();
    Account account = PublicMethed.queryAccount(frozenAddress,blockingStubFull);
    Assert.assertEquals(account.getKcoinPower().getFrozenBalance(),frozenAmountForKcoinPower);
    Assert.assertTrue(account.getKcoinPower().getExpireTime() > beforeFrozenTime
        && account.getKcoinPower().getExpireTime() < afterFrozenTime);

    accountResource = PublicMethed
        .getAccountResource(frozenAddress, blockingStubFull);
    Long afterTotalKcoinPowerWeight = accountResource.getTotalKcoinPowerWeight();
    Long afterKcoinPowerLimit = accountResource.getKcoinPowerLimit();
    Long afterKcoinPowerUsed = accountResource.getKcoinPowerUsed();
    Assert.assertEquals(afterTotalKcoinPowerWeight - beforeTotalKcoinPowerWeight,
        frozenAmountForKcoinPower / 1000000L);

    Assert.assertEquals(afterKcoinPowerLimit - beforeKcoinPowerLimit,
        frozenAmountForKcoinPower / 1000000L);



    Assert.assertTrue(PublicMethed.freezeBalanceGetKcoinPower(frozenAddress,
        6000000 - frozenAmountForKcoinPower,
        0,2,null,frozenKey,blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    accountResource = PublicMethed
        .getAccountResource(frozenAddress, blockingStubFull);
    afterKcoinPowerLimit = accountResource.getKcoinPowerLimit();

    Assert.assertEquals(afterKcoinPowerLimit - beforeKcoinPowerLimit,
        6);


  }


  @Test(enabled = true,description = "Vote witness by kcoin power")
  public void test02VotePowerOnlyComeFromKcoinPower() {
    AccountResourceMessage accountResource = PublicMethed
        .getAccountResource(frozenAddress, blockingStubFull);
    final Long beforeKcoinPowerUsed = accountResource.getKcoinPowerUsed();


    HashMap<byte[],Long> witnessMap = new HashMap<>();
    witnessMap.put(witnessAddress,frozenAmountForNet / 1000000L);
    Assert.assertFalse(PublicMethed.voteWitness(frozenAddress,frozenKey,witnessMap,
        blockingStubFull));
    witnessMap.put(witnessAddress,frozenAmountForKcoinPower / 1000000L);
    Assert.assertTrue(PublicMethed.voteWitness(frozenAddress,frozenKey,witnessMap,
        blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    accountResource = PublicMethed
        .getAccountResource(frozenAddress, blockingStubFull);
    Long afterKcoinPowerUsed = accountResource.getKcoinPowerUsed();
    Assert.assertEquals(afterKcoinPowerUsed - beforeKcoinPowerUsed,
        frozenAmountForKcoinPower / 1000000L);

    final Long secondBeforeKcoinPowerUsed = afterKcoinPowerUsed;
    witnessMap.put(witnessAddress,(frozenAmountForKcoinPower / 1000000L) - 1);
    Assert.assertTrue(PublicMethed.voteWitness(frozenAddress,frozenKey,witnessMap,
        blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    accountResource = PublicMethed
        .getAccountResource(frozenAddress, blockingStubFull);
    afterKcoinPowerUsed = accountResource.getKcoinPowerUsed();
    Assert.assertEquals(secondBeforeKcoinPowerUsed - afterKcoinPowerUsed,
        1);


  }

  @Test(enabled = true,description = "Kcoin power is not allow to others")
  public void test03KcoinPowerIsNotAllowToOthers() {
    Assert.assertFalse(PublicMethed.freezeBalanceGetKcoinPower(frozenAddress,
        frozenAmountForKcoinPower, 0,2,
        ByteString.copyFrom(foundationAddress),frozenKey,blockingStubFull));
  }


  @Test(enabled = true,description = "Unfreeze balance for kcoin power")
  public void test04UnfreezeBalanceForKcoinPower() {
    AccountResourceMessage accountResource = PublicMethed
        .getAccountResource(foundationAddress, blockingStubFull);
    final Long beforeTotalKcoinPowerWeight = accountResource.getTotalKcoinPowerWeight();


    Assert.assertTrue(PublicMethed.unFreezeBalance(frozenAddress,frozenKey,2,
        null,blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    accountResource = PublicMethed
        .getAccountResource(frozenAddress, blockingStubFull);
    Long afterTotalKcoinPowerWeight = accountResource.getTotalKcoinPowerWeight();
    Assert.assertEquals(beforeTotalKcoinPowerWeight - afterTotalKcoinPowerWeight,
        6);

    Assert.assertEquals(accountResource.getKcoinPowerLimit(),0L);
    Assert.assertEquals(accountResource.getKcoinPowerUsed(),0L);

    Account account = PublicMethed.queryAccount(frozenAddress,blockingStubFull);
    Assert.assertEquals(account.getKcoinPower().getFrozenBalance(),0);


  }
  

  /**
   * constructor.
   */

  @AfterClass(enabled = true)
  public void shutdown() throws InterruptedException {
    PublicMethed.unFreezeBalance(frozenAddress, frozenKey, 2, null,
        blockingStubFull);
    PublicMethed.unFreezeBalance(frozenAddress, frozenKey, 0, null,
        blockingStubFull);
    PublicMethed.freedResource(frozenAddress, frozenKey, foundationAddress, blockingStubFull);
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}


