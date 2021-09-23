package org.kcoin.core.net.message;

import java.util.List;
import org.kcoin.core.capsule.TransactionCapsule;
import org.kcoin.protos.Protocol;
import org.kcoin.protos.Protocol.Transaction;

public class TransactionsMessage extends KcoinMessage {

  private Protocol.Transactions transactions;

  public TransactionsMessage(List<Transaction> syms) {
    Protocol.Transactions.Builder builder = Protocol.Transactions.newBuilder();
    syms.forEach(sym -> builder.addTransactions(sym));
    this.transactions = builder.build();
    this.type = MessageTypes.SYMS.asByte();
    this.data = this.transactions.toByteArray();
  }

  public TransactionsMessage(byte[] data) throws Exception {
    super(data);
    this.type = MessageTypes.SYMS.asByte();
    this.transactions = Protocol.Transactions.parseFrom(getCodedInputStream(data));
    if (isFilter()) {
      compareBytes(data, transactions.toByteArray());
      TransactionCapsule.validContractProto(transactions.getTransactionsList());
    }
  }

  public Protocol.Transactions getTransactions() {
    return transactions;
  }

  @Override
  public String toString() {
    return new StringBuilder().append(super.toString()).append("sym size: ")
        .append(this.transactions.getTransactionsList().size()).toString();
  }

  @Override
  public Class<?> getAnswerMessage() {
    return null;
  }

}
