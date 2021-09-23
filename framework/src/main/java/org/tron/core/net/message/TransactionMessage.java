package org.kcoin.core.net.message;

import org.kcoin.common.overlay.message.Message;
import org.kcoin.common.utils.Sha256Hash;
import org.kcoin.core.capsule.TransactionCapsule;
import org.kcoin.protos.Protocol.Transaction;

public class TransactionMessage extends KcoinMessage {

  private TransactionCapsule transactionCapsule;

  public TransactionMessage(byte[] data) throws Exception {
    super(data);
    this.transactionCapsule = new TransactionCapsule(getCodedInputStream(data));
    this.type = MessageTypes.SYM.asByte();
    if (Message.isFilter()) {
      compareBytes(data, transactionCapsule.getInstance().toByteArray());
      transactionCapsule
          .validContractProto(transactionCapsule.getInstance().getRawData().getContract(0));
    }
  }

  public TransactionMessage(Transaction sym) {
    this.transactionCapsule = new TransactionCapsule(sym);
    this.type = MessageTypes.SYM.asByte();
    this.data = sym.toByteArray();
  }

  @Override
  public String toString() {
    return new StringBuilder().append(super.toString())
        .append("messageId: ").append(super.getMessageId()).toString();
  }

  @Override
  public Sha256Hash getMessageId() {
    return this.transactionCapsule.getTransactionId();
  }

  @Override
  public Class<?> getAnswerMessage() {
    return null;
  }

  public TransactionCapsule getTransactionCapsule() {
    return this.transactionCapsule;
  }
}
