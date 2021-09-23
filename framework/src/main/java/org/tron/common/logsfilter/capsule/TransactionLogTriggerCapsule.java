package org.kcoin.common.logsfilter.capsule;

import static org.kcoin.protos.Protocol.Transaction.Contract.ContractType.TransferAssetContract;
import static org.kcoin.protos.Protocol.Transaction.Contract.ContractType.TransferContract;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.kcoin.common.logsfilter.EventPluginLoader;
import org.kcoin.common.logsfilter.trigger.InternalTransactionPojo;
import org.kcoin.common.logsfilter.trigger.TransactionLogTrigger;
import org.kcoin.common.runtime.InternalTransaction;
import org.kcoin.common.runtime.ProgramResult;
import org.kcoin.common.utils.StringUtil;
import org.kcoin.core.capsule.BlockCapsule;
import org.kcoin.core.capsule.TransactionCapsule;
import org.kcoin.core.db.TransactionTrace;
import org.kcoin.protos.Protocol;
import org.kcoin.protos.contract.AssetIssueContractOuterClass.TransferAssetContract;
import org.kcoin.protos.contract.BalanceContract.TransferContract;

@Slf4j
public class TransactionLogTriggerCapsule extends TriggerCapsule {

  @Getter
  @Setter
  private TransactionLogTrigger transactionLogTrigger;

  public TransactionLogTriggerCapsule(TransactionCapsule symCapsule, BlockCapsule blockCapsule) {
    transactionLogTrigger = new TransactionLogTrigger();
    if (Objects.nonNull(blockCapsule)) {
      transactionLogTrigger.setBlockHash(blockCapsule.getBlockId().toString());
    }
    transactionLogTrigger.setTransactionId(symCapsule.getTransactionId().toString());
    transactionLogTrigger.setTimeStamp(blockCapsule.getTimeStamp());
    transactionLogTrigger.setBlockNumber(symCapsule.getBlockNum());
    transactionLogTrigger.setData(Hex.toHexString(symCapsule
        .getInstance().getRawData().getData().toByteArray()));

    TransactionTrace symTrace = symCapsule.getSymTrace();

    //result
    if (Objects.nonNull(symCapsule.getContractRet())) {
      transactionLogTrigger.setResult(symCapsule.getContractRet().toString());
    }

    if (Objects.nonNull(symCapsule.getInstance().getRawData())) {
      // fee limit
      transactionLogTrigger.setFeeLimit(symCapsule.getInstance().getRawData().getFeeLimit());

      Protocol.Transaction.Contract contract = symCapsule.getInstance().getRawData().getContract(0);
      Any contractParameter = null;
      // contract type
      if (Objects.nonNull(contract)) {
        Protocol.Transaction.Contract.ContractType contractType = contract.getType();
        if (Objects.nonNull(contractType)) {
          transactionLogTrigger.setContractType(contractType.toString());
        }

        contractParameter = contract.getParameter();

        transactionLogTrigger.setContractCallValue(TransactionCapsule.getCallValue(contract));
      }

      if (Objects.nonNull(contractParameter) && Objects.nonNull(contract)) {
        try {
          if (contract.getType() == TransferContract) {
            TransferContract contractTransfer = contractParameter.unpack(TransferContract.class);

            if (Objects.nonNull(contractTransfer)) {
              transactionLogTrigger.setAssetName("sym");

              if (Objects.nonNull(contractTransfer.getOwnerAddress())) {
                transactionLogTrigger.setFromAddress(StringUtil
                    .encode58Check(contractTransfer.getOwnerAddress().toByteArray()));
              }

              if (Objects.nonNull(contractTransfer.getToAddress())) {
                transactionLogTrigger.setToAddress(
                    StringUtil.encode58Check(contractTransfer.getToAddress().toByteArray()));
              }

              transactionLogTrigger.setAssetAmount(contractTransfer.getAmount());
            }

          } else if (contract.getType() == TransferAssetContract) {
            TransferAssetContract contractTransfer = contractParameter
                .unpack(TransferAssetContract.class);

            if (Objects.nonNull(contractTransfer)) {
              if (Objects.nonNull(contractTransfer.getAssetName())) {
                transactionLogTrigger.setAssetName(contractTransfer.getAssetName().toStringUtf8());
              }

              if (Objects.nonNull(contractTransfer.getOwnerAddress())) {
                transactionLogTrigger.setFromAddress(
                    StringUtil.encode58Check(contractTransfer.getOwnerAddress().toByteArray()));
              }

              if (Objects.nonNull(contractTransfer.getToAddress())) {
                transactionLogTrigger.setToAddress(StringUtil
                    .encode58Check(contractTransfer.getToAddress().toByteArray()));
              }
              transactionLogTrigger.setAssetAmount(contractTransfer.getAmount());
            }
          }
        } catch (Exception e) {
          logger.error("failed to load transferAssetContract, error'{}'", e);
        }
      }
    }

    // receipt
    if (Objects.nonNull(symTrace) && Objects.nonNull(symTrace.getReceipt())) {
      transactionLogTrigger.setEnergyFee(symTrace.getReceipt().getEnergyFee());
      transactionLogTrigger.setOriginEnergyUsage(symTrace.getReceipt().getOriginEnergyUsage());
      transactionLogTrigger.setEnergyUsageTotal(symTrace.getReceipt().getEnergyUsageTotal());
      transactionLogTrigger.setNetUsage(symTrace.getReceipt().getNetUsage());
      transactionLogTrigger.setNetFee(symTrace.getReceipt().getNetFee());
      transactionLogTrigger.setEnergyUsage(symTrace.getReceipt().getEnergyUsage());
    }

    // program result
    if (Objects.nonNull(symTrace) && Objects.nonNull(symTrace.getRuntime()) && Objects
        .nonNull(symTrace.getRuntime().getResult())) {
      ProgramResult programResult = symTrace.getRuntime().getResult();
      ByteString contractResult = ByteString.copyFrom(programResult.getHReturn());
      ByteString contractAddress = ByteString.copyFrom(programResult.getContractAddress());

      if (Objects.nonNull(contractResult) && contractResult.size() > 0) {
        transactionLogTrigger.setContractResult(Hex.toHexString(contractResult.toByteArray()));
      }

      if (Objects.nonNull(contractAddress) && contractAddress.size() > 0) {
        transactionLogTrigger
            .setContractAddress(StringUtil.encode58Check((contractAddress.toByteArray())));
      }

      // internal transaction
      transactionLogTrigger.setInternalTransactionList(
          getInternalTransactionList(programResult.getInternalTransactions()));
    }
  }

  public void setLatestSolidifiedBlockNumber(long latestSolidifiedBlockNumber) {
    transactionLogTrigger.setLatestSolidifiedBlockNumber(latestSolidifiedBlockNumber);
  }

  private List<InternalTransactionPojo> getInternalTransactionList(
      List<InternalTransaction> internalTransactionList) {
    List<InternalTransactionPojo> pojoList = new ArrayList<>();

    internalTransactionList.forEach(internalTransaction -> {
      InternalTransactionPojo item = new InternalTransactionPojo();

      item.setHash(Hex.toHexString(internalTransaction.getHash()));
      item.setCallValue(internalTransaction.getValue());
      item.setTokenInfo(internalTransaction.getTokenInfo());
      item.setCaller_address(Hex.toHexString(internalTransaction.getSender()));
      item.setTransferTo_address(Hex.toHexString(internalTransaction.getTransferToAddress()));
      item.setData(Hex.toHexString(internalTransaction.getData()));
      item.setRejected(internalTransaction.isRejected());
      item.setNote(internalTransaction.getNote());
      item.setExtra(internalTransaction.getExtra());

      pojoList.add(item);
    });

    return pojoList;
  }

  @Override
  public void processTrigger() {
    EventPluginLoader.getInstance().postTransactionTrigger(transactionLogTrigger);
  }
}
