package org.kcoin.common.runtime;

import static org.kcoin.common.runtime.TvmTestUtils.generateDeploySmartContractAndGetTransaction;
import static org.kcoin.common.runtime.TvmTestUtils.generateTriggerSmartContractAndGetTransaction;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testng.Assert;
import org.kcoin.common.application.Application;
import org.kcoin.common.application.ApplicationFactory;
import org.kcoin.common.application.KcoinApplicationContext;
import org.kcoin.common.utils.FileUtil;
import org.kcoin.core.Constant;
import org.kcoin.core.Wallet;
import org.kcoin.core.actuator.VMActuator;
import org.kcoin.core.capsule.AccountCapsule;
import org.kcoin.core.capsule.ContractCapsule;
import org.kcoin.core.capsule.TransactionCapsule;
import org.kcoin.core.config.DefaultConfig;
import org.kcoin.core.config.args.Args;
import org.kcoin.core.db.Manager;
import org.kcoin.core.db.TransactionContext;
import org.kcoin.core.exception.ContractExeException;
import org.kcoin.core.exception.ContractValidateException;
import org.kcoin.core.exception.ReceiptCheckErrException;
import org.kcoin.core.exception.VMIllegalException;
import org.kcoin.core.store.StoreFactory;
import org.kcoin.core.vm.repository.Repository;
import org.kcoin.core.vm.repository.RepositoryImpl;
import org.kcoin.protos.Protocol.AccountType;
import org.kcoin.protos.Protocol.Transaction;
import org.kcoin.protos.contract.SmartContractOuterClass.TriggerSmartContract;


@Slf4j

public class RuntimeImplTest {

  private Manager dbManager;
  private KcoinApplicationContext context;
  private Repository repository;
  private String dbPath = "output_RuntimeImplTest";
  private Application AppT;
  private byte[] callerAddress;
  private long callerTotalBalance = 4_000_000_000L;
  private byte[] creatorAddress;
  private long creatorTotalBalance = 3_000_000_000L;

  /**
   * Init data.
   */
  @Before
  public void init() {
    Args.setParam(new String[]{"--output-directory", dbPath, "--debug"}, Constant.TEST_CONF);
    context = new KcoinApplicationContext(DefaultConfig.class);
    AppT = ApplicationFactory.create(context);
    callerAddress = Hex
        .decode(Wallet.getAddressPreFixString() + "abd4b9367799eaa3197fecb144eb71de1e049abc");
    creatorAddress = Hex
        .decode(Wallet.getAddressPreFixString() + "abd4b9367799eaa3197fecb144eb71de1e049abd");
    dbManager = context.getBean(Manager.class);
    dbManager.getDynamicPropertiesStore().saveLatestBlockHeaderTimestamp(1526647838000L);
    dbManager.getDynamicPropertiesStore().saveTotalEnergyWeight(5_000_000_000L); // unit is sym
    repository = RepositoryImpl.createRoot(StoreFactory.getInstance());
    repository.createAccount(callerAddress, AccountType.Normal);
    repository.addBalance(callerAddress, callerTotalBalance);
    repository.createAccount(creatorAddress, AccountType.Normal);
    repository.addBalance(creatorAddress, creatorTotalBalance);
    repository.commit();
  }

  // // solidity src code
  // pragma solidity ^0.4.2;
  //
  // contract TestEnergyLimit {
  //
  //   function testNotConstant(uint256 count) {
  //     uint256 curCount = 0;
  //     while(curCount < count) {
  //       uint256 a = 1;
  //       curCount += 1;
  //     }
  //   }
  //
  //   function testConstant(uint256 count) constant {
  //     uint256 curCount = 0;
  //     while(curCount < count) {
  //       uint256 a = 1;
  //       curCount += 1;
  //     }
  //   }
  //
  // }


  @Test
  public void getCreatorEnergyLimit2Test() throws ContractValidateException, ContractExeException {

    long value = 10L;
    long feeLimit = 1_000_000_000L;
    long consumeUserResourcePercent = 0L;
    String contractName = "test";
    String ABI = "[{\"constant\":true,\"inputs\":[{\"name\":\"count\",\"type\":\"uint256\"}],\""
        + "name\":\"testConstant\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"view"
        + "\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"count\",\"type"
        + "\":\"uint256\"}],\"name\":\"testNotConstant\",\"outputs\":[],\"payable\":false,\""
        + "stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    String code = "608060405234801561001057600080fd5b50610112806100206000396000f300608060405260043"
        + "6106049576000357c0100000000000000000000000000000000000000000000000000000000900463ffffff"
        + "ff16806321964a3914604e5780634c6bb6eb146078575b600080fd5b348015605957600080fd5b506076600"
        + "4803603810190808035906020019092919050505060a2565b005b348015608357600080fd5b5060a0600480"
        + "3603810190808035906020019092919050505060c4565b005b600080600091505b8282101560bf576001905"
        + "060018201915060aa565b505050565b600080600091505b8282101560e1576001905060018201915060cc56"
        + "5b5050505600a165627a7a72305820267cf0ebf31051a92ff62bed7490045b8063be9f1e1a22d07dce25765"
        + "4c8c17b0029";
    String libraryAddressPair = null;

    Transaction sym = generateDeploySmartContractAndGetTransaction(contractName, creatorAddress,
        ABI,
        code, value, feeLimit, consumeUserResourcePercent, libraryAddressPair);

    RuntimeImpl runtimeImpl = new RuntimeImpl();
    runtimeImpl.execute(
        new TransactionContext(null, new TransactionCapsule(sym),
            StoreFactory.getInstance(), true, true));

    repository = RepositoryImpl.createRoot(StoreFactory.getInstance());
    AccountCapsule creatorAccount = repository.getAccount(creatorAddress);

    long expectEnergyLimit1 = 10_000_000L;
    Assert.assertEquals(
        ((VMActuator) runtimeImpl.getActuator2())
            .getAccountEnergyLimitWithFixRatio(creatorAccount, feeLimit, value),
        expectEnergyLimit1);

    value = 2_500_000_000L;
    long expectEnergyLimit2 = 5_000_000L;
    Assert.assertEquals(
        ((VMActuator) runtimeImpl.getActuator2())
            .getAccountEnergyLimitWithFixRatio(creatorAccount, feeLimit, value),
        expectEnergyLimit2);

    value = 10L;
    feeLimit = 1_000_000L;
    long expectEnergyLimit3 = 10_000L;
    Assert.assertEquals(
        ((VMActuator) runtimeImpl.getActuator2())
            .getAccountEnergyLimitWithFixRatio(creatorAccount, feeLimit, value),
        expectEnergyLimit3);

    long frozenBalance = 1_000_000_000L;
    long newBalance = creatorAccount.getBalance() - frozenBalance;
    creatorAccount.setFrozenForEnergy(frozenBalance, 0L);
    creatorAccount.setBalance(newBalance);
    repository.putAccountValue(creatorAddress, creatorAccount);
    repository.commit();

    feeLimit = 1_000_000_000L;
    long expectEnergyLimit4 = 10_000_000L;
    Assert.assertEquals(
        ((VMActuator) runtimeImpl.getActuator2())
            .getAccountEnergyLimitWithFixRatio(creatorAccount, feeLimit, value),
        expectEnergyLimit4);

    feeLimit = 3_000_000_000L;
    value = 10L;
    long expectEnergyLimit5 = 20_009_999L;
    Assert.assertEquals(
        ((VMActuator) runtimeImpl.getActuator2())
            .getAccountEnergyLimitWithFixRatio(creatorAccount, feeLimit, value),
        expectEnergyLimit5);

    feeLimit = 3_000L;
    value = 10L;
    long expectEnergyLimit6 = 30L;
    Assert.assertEquals(
        ((VMActuator) runtimeImpl.getActuator2())
            .getAccountEnergyLimitWithFixRatio(creatorAccount, feeLimit, value),
        expectEnergyLimit6);

  }

  @Test
  public void getCallerAndCreatorEnergyLimit2With0PercentTest()
      throws ContractExeException, ReceiptCheckErrException, VMIllegalException,
      ContractValidateException {

    long value = 0;
    long feeLimit = 1_000_000_000L; // sun
    long consumeUserResourcePercent = 0L;
    long creatorEnergyLimit = 5_000L;
    String contractName = "test";
    String ABI = "[{\"constant\":true,\"inputs\":[{\"name\":\"count\",\"type\":\"uint256\"}],"
        + "\"name\":\"testConstant\",\"outputs\":[],\"payable\":false,\"stateMutability\":\""
        + "view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"count\","
        + "\"type\":\"uint256\"}],\"name\":\"testNotConstant\",\"outputs\":[],\"payable\""
        + ":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    String code = "608060405234801561001057600080fd5b50610112806100206000396000f300608060405260043"
        + "6106049576000357c0100000000000000000000000000000000000000000000000000000000900463ffffff"
        + "ff16806321964a3914604e5780634c6bb6eb146078575b600080fd5b348015605957600080fd5b506076600"
        + "4803603810190808035906020019092919050505060a2565b005b348015608357600080fd5b5060a0600480"
        + "3603810190808035906020019092919050505060c4565b005b600080600091505b8282101560bf576001905"
        + "060018201915060aa565b505050565b600080600091505b8282101560e1576001905060018201915060cc56"
        + "5b5050505600a165627a7a72305820267cf0ebf31051a92ff62bed7490045b8063be9f1e1a22d07dce25765"
        + "4c8c17b0029";
    String libraryAddressPair = null;
    TVMTestResult result = TvmTestUtils
        .deployContractWithCreatorEnergyLimitAndReturnTvmTestResult(contractName, creatorAddress,
            ABI, code, value,
            feeLimit, consumeUserResourcePercent, libraryAddressPair, dbManager, null,
            creatorEnergyLimit);

    byte[] contractAddress = result.getContractAddress();
    byte[] triggerData = TvmTestUtils.parseAbi("testNotConstant()", null);
    Transaction sym = generateTriggerSmartContractAndGetTransaction(callerAddress, contractAddress,
        triggerData, value, feeLimit);

    repository = RepositoryImpl.createRoot(StoreFactory.getInstance());
    RuntimeImpl runtimeImpl = new RuntimeImpl();
    runtimeImpl.execute(
        new TransactionContext(null, new TransactionCapsule(sym),
            StoreFactory.getInstance(), true, true));

    AccountCapsule creatorAccount = repository.getAccount(creatorAddress);
    AccountCapsule callerAccount = repository.getAccount(callerAddress);
    TriggerSmartContract contract = ContractCapsule.getTriggerContractFromTransaction(sym);

    feeLimit = 1_000_000_000L;
    value = 0L;
    long expectEnergyLimit1 = 10_000_000L;
    Assert.assertEquals(
        ((VMActuator) runtimeImpl.getActuator2())
            .getTotalEnergyLimitWithFixRatio(creatorAccount, callerAccount, contract, feeLimit,
                value),
        expectEnergyLimit1);

    long creatorFrozenBalance = 1_000_000_000L;
    long newBalance = creatorAccount.getBalance() - creatorFrozenBalance;
    creatorAccount.setFrozenForEnergy(creatorFrozenBalance, 0L);
    creatorAccount.setBalance(newBalance);
    repository.putAccountValue(creatorAddress, creatorAccount);
    repository.commit();

    feeLimit = 1_000_000_000L;
    value = 0L;
    long expectEnergyLimit2 = 10_005_000L;
    Assert.assertEquals(
        ((VMActuator) runtimeImpl.getActuator2())
            .getTotalEnergyLimitWithFixRatio(creatorAccount, callerAccount, contract, feeLimit,
                value),
        expectEnergyLimit2);

    value = 3_500_000_000L;
    long expectEnergyLimit3 = 5_005_000L;
    Assert.assertEquals(
        ((VMActuator) runtimeImpl.getActuator2())
            .getTotalEnergyLimitWithFixRatio(creatorAccount, callerAccount, contract, feeLimit,
                value),
        expectEnergyLimit3);

    value = 10L;
    feeLimit = 5_000_000_000L;
    long expectEnergyLimit4 = 40_004_999L;
    Assert.assertEquals(
        ((VMActuator) runtimeImpl.getActuator2())
            .getTotalEnergyLimitWithFixRatio(creatorAccount, callerAccount, contract, feeLimit,
                value),
        expectEnergyLimit4);

    long callerFrozenBalance = 1_000_000_000L;
    callerAccount.setFrozenForEnergy(callerFrozenBalance, 0L);
    callerAccount.setBalance(callerAccount.getBalance() - callerFrozenBalance);
    repository.putAccountValue(callerAddress, callerAccount);
    repository.commit();

    value = 10L;
    feeLimit = 5_000_000_000L;
    long expectEnergyLimit5 = 30_014_999L;
    Assert.assertEquals(
        ((VMActuator) runtimeImpl.getActuator2())
            .getTotalEnergyLimitWithFixRatio(creatorAccount, callerAccount, contract, feeLimit,
                value),
        expectEnergyLimit5);

  }

  @Test
  public void getCallerAndCreatorEnergyLimit2With40PercentTest()
      throws ContractExeException, ReceiptCheckErrException, VMIllegalException,
      ContractValidateException {

    long value = 0;
    long feeLimit = 1_000_000_000L; // sun
    long consumeUserResourcePercent = 40L;
    long creatorEnergyLimit = 5_000L;
    String contractName = "test";
    String ABI = "[{\"constant\":true,\"inputs\":[{\"name\":\"count\",\"type\":\"uint256\"}],\""
        + "name\":\"testConstant\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"view\""
        + ",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"count\",\"type\":"
        + "\"uint256\"}],\"name\":\"testNotConstant\",\"outputs\":[],\"payable\":false,\""
        + "stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    String code = "608060405234801561001057600080fd5b50610112806100206000396000f300608060405260043"
        + "6106049576000357c0100000000000000000000000000000000000000000000000000000000900463ffffff"
        + "ff16806321964a3914604e5780634c6bb6eb146078575b600080fd5b348015605957600080fd5b50607660"
        + "04803603810190808035906020019092919050505060a2565b005b348015608357600080fd5b5060a060048"
        + "03603810190808035906020019092919050505060c4565b005b600080600091505b8282101560bf57600190"
        + "5060018201915060aa565b505050565b600080600091505b8282101560e1576001905060018201915060cc5"
        + "65b5050505600a165627a7a72305820267cf0ebf31051a92ff62bed7490045b8063be9f1e1a22d07dce2576"
        + "54c8c17b0029";
    String libraryAddressPair = null;
    TVMTestResult result = TvmTestUtils
        .deployContractWithCreatorEnergyLimitAndReturnTvmTestResult(contractName, creatorAddress,
            ABI, code, value,
            feeLimit, consumeUserResourcePercent, libraryAddressPair, dbManager, null,
            creatorEnergyLimit);

    byte[] contractAddress = result.getContractAddress();
    byte[] triggerData = TvmTestUtils.parseAbi("testNotConstant()", null);
    Transaction sym = generateTriggerSmartContractAndGetTransaction(callerAddress, contractAddress,
        triggerData, value, feeLimit);

    repository = RepositoryImpl.createRoot(StoreFactory.getInstance());
    RuntimeImpl runtimeImpl = new RuntimeImpl();
    runtimeImpl.execute(
        new TransactionContext(null, new TransactionCapsule(sym),
            StoreFactory.getInstance(), true, true));

    AccountCapsule creatorAccount = repository.getAccount(creatorAddress);
    AccountCapsule callerAccount = repository.getAccount(callerAddress);
    TriggerSmartContract contract = ContractCapsule.getTriggerContractFromTransaction(sym);

    feeLimit = 1_000_000_000L;
    value = 0L;
    long expectEnergyLimit1 = 10_000_000L;
    Assert.assertEquals(
        ((VMActuator) runtimeImpl.getActuator2())
            .getTotalEnergyLimitWithFixRatio(creatorAccount, callerAccount, contract, feeLimit,
                value),
        expectEnergyLimit1);

    long creatorFrozenBalance = 1_000_000_000L;
    long newBalance = creatorAccount.getBalance() - creatorFrozenBalance;
    creatorAccount.setFrozenForEnergy(creatorFrozenBalance, 0L);
    creatorAccount.setBalance(newBalance);
    repository.putAccountValue(creatorAddress, creatorAccount);
    repository.commit();

    feeLimit = 1_000_000_000L;
    value = 0L;
    long expectEnergyLimit2 = 10_005_000L;
    Assert.assertEquals(
        ((VMActuator) runtimeImpl.getActuator2())
            .getTotalEnergyLimitWithFixRatio(creatorAccount, callerAccount, contract, feeLimit,
                value),
        expectEnergyLimit2);

    value = 3_999_950_000L;
    long expectEnergyLimit3 = 1_250L;
    Assert.assertEquals(
        ((VMActuator) runtimeImpl.getActuator2())
            .getTotalEnergyLimitWithFixRatio(creatorAccount, callerAccount, contract, feeLimit,
                value),
        expectEnergyLimit3);

  }

  @Test
  public void getCallerAndCreatorEnergyLimit2With100PercentTest()
      throws ContractExeException, ReceiptCheckErrException, VMIllegalException,
      ContractValidateException {

    long value = 0;
    long feeLimit = 1_000_000_000L; // sun
    long consumeUserResourcePercent = 100L;
    long creatorEnergyLimit = 5_000L;
    String contractName = "test";
    String ABI = "[{\"constant\":true,\"inputs\":[{\"name\":\"count\",\"type\":\"uint256\"}],"
        + "\"name\":\"testConstant\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"view\""
        + ",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"count\",\"type\":\""
        + "uint256\"}],\"name\":\"testNotConstant\",\"outputs\":[],\"payable\":false,\""
        + "stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    String code = "608060405234801561001057600080fd5b50610112806100206000396000f300608060405260043"
        + "6106049576000357c0100000000000000000000000000000000000000000000000000000000900463ffffff"
        + "ff16806321964a3914604e5780634c6bb6eb146078575b600080fd5b348015605957600080fd5b506076600"
        + "4803603810190808035906020019092919050505060a2565b005b348015608357600080fd5b5060a0600480"
        + "3603810190808035906020019092919050505060c4565b005b600080600091505b8282101560bf576001905"
        + "060018201915060aa565b505050565b600080600091505b8282101560e1576001905060018201915060cc56"
        + "5b5050505600a165627a7a72305820267cf0ebf31051a92ff62bed7490045b8063be9f1e1a22d07dce25765"
        + "4c8c17b0029";
    String libraryAddressPair = null;
    TVMTestResult result = TvmTestUtils
        .deployContractWithCreatorEnergyLimitAndReturnTvmTestResult(contractName, creatorAddress,
            ABI, code, value,
            feeLimit, consumeUserResourcePercent, libraryAddressPair, dbManager, null,
            creatorEnergyLimit);

    byte[] contractAddress = result.getContractAddress();
    byte[] triggerData = TvmTestUtils.parseAbi("testNotConstant()", null);
    Transaction sym = generateTriggerSmartContractAndGetTransaction(callerAddress, contractAddress,
        triggerData, value, feeLimit);

    repository = RepositoryImpl.createRoot(StoreFactory.getInstance());
    RuntimeImpl runtimeImpl = new RuntimeImpl();
    runtimeImpl.execute(
        new TransactionContext(null, new TransactionCapsule(sym),
            StoreFactory.getInstance(), true, true));

    AccountCapsule creatorAccount = repository.getAccount(creatorAddress);
    AccountCapsule callerAccount = repository.getAccount(callerAddress);
    TriggerSmartContract contract = ContractCapsule.getTriggerContractFromTransaction(sym);

    feeLimit = 1_000_000_000L;
    value = 0L;
    long expectEnergyLimit1 = 10_000_000L;
    Assert.assertEquals(
        ((VMActuator) runtimeImpl.getActuator2())
            .getTotalEnergyLimitWithFixRatio(creatorAccount, callerAccount, contract, feeLimit,
                value),
        expectEnergyLimit1);

    long creatorFrozenBalance = 1_000_000_000L;
    long newBalance = creatorAccount.getBalance() - creatorFrozenBalance;
    creatorAccount.setFrozenForEnergy(creatorFrozenBalance, 0L);
    creatorAccount.setBalance(newBalance);
    repository.putAccountValue(creatorAddress, creatorAccount);
    repository.commit();

    feeLimit = 1_000_000_000L;
    value = 0L;
    long expectEnergyLimit2 = 10_000_000L;
    Assert.assertEquals(
        ((VMActuator) runtimeImpl.getActuator2())
            .getTotalEnergyLimitWithFixRatio(creatorAccount, callerAccount, contract, feeLimit,
                value),
        expectEnergyLimit2);

    value = 3_999_950_000L;
    long expectEnergyLimit3 = 500L;
    Assert.assertEquals(
        ((VMActuator) runtimeImpl.getActuator2())
            .getTotalEnergyLimitWithFixRatio(creatorAccount, callerAccount, contract, feeLimit,
                value),
        expectEnergyLimit3);

  }

  /**
   * Release resources.
   */
  @After
  public void destroy() {
    Args.clearParam();
    context.destroy();
    if (FileUtil.deleteDir(new File(dbPath))) {
      logger.info("Release resources successful.");
    } else {
      logger.info("Release resources failure.");
    }
  }
}

