package org.kcoin.consensus.pbft.message;

import com.google.protobuf.ByteString;
import java.util.List;
import org.kcoin.common.crypto.ECKey;
import org.kcoin.common.crypto.ECKey.ECDSASignature;
import org.kcoin.common.utils.Sha256Hash;
import org.kcoin.consensus.base.Param;
import org.kcoin.consensus.base.Param.Miner;
import org.kcoin.core.capsule.BlockCapsule;
import org.kcoin.core.net.message.MessageTypes;
import org.kcoin.protos.Protocol.PBFTMessage;
import org.kcoin.protos.Protocol.PBFTMessage.DataType;
import org.kcoin.protos.Protocol.PBFTMessage.MsgType;
import org.kcoin.protos.Protocol.PBFTMessage.Raw;
import org.kcoin.protos.Protocol.SRL;

public class PbftMessage extends PbftBaseMessage {

  public PbftMessage() {
  }

  public PbftMessage(byte[] data) throws Exception {
    super(MessageTypes.PBFT_MSG.asByte(), data);
  }

  public String getNo() {
    return pbftMessage.getRawData().getViewN() + "_" + pbftMessage.getRawData().getDataType();
  }

  public static PbftMessage prePrepareBlockMsg(BlockCapsule block, long epoch, Miner miner) {
    return buildCommon(DataType.BLOCK, block.getBlockId().getByteString(), block, epoch,
        block.getNum(), miner);
  }

  public static PbftMessage fullNodePrePrepareBlockMsg(BlockCapsule block,
      long epoch) {
    return buildFullNodeCommon(DataType.BLOCK, block.getBlockId().getByteString(), block, epoch,
        block.getNum());
  }

  public static PbftMessage prePrepareSRLMsg(BlockCapsule block,
      List<ByteString> currentWitness, long epoch, Miner miner) {
    SRL.Builder srListBuilder = SRL.newBuilder();
    ByteString data = srListBuilder.addAllSrAddress(currentWitness).build().toByteString();
    return buildCommon(DataType.SRL, data, block, epoch, epoch, miner);
  }

  public static PbftMessage fullNodePrePrepareSRLMsg(BlockCapsule block,
      List<ByteString> currentWitness, long epoch) {
    SRL.Builder srListBuilder = SRL.newBuilder();
    ByteString data = srListBuilder.addAllSrAddress(currentWitness).build().toByteString();
    return buildFullNodeCommon(DataType.SRL, data, block, epoch, epoch);
  }

  private static PbftMessage buildCommon(DataType dataType, ByteString data, BlockCapsule block,
      long epoch, long viewN, Miner miner) {
    PbftMessage pbftMessage = new PbftMessage();
    ECKey ecKey = ECKey.fromPrivate(miner.getPrivateKey());
    Raw.Builder rawBuilder = Raw.newBuilder();
    PBFTMessage.Builder builder = PBFTMessage.newBuilder();
    rawBuilder.setViewN(viewN).setEpoch(epoch).setDataType(dataType)
        .setMsgType(MsgType.PREPREPARE).setData(data);
    Raw raw = rawBuilder.build();
    byte[] hash = Sha256Hash.hash(true, raw.toByteArray());
    ECDSASignature signature = ecKey.sign(hash);
    builder.setRawData(raw).setSignature(ByteString.copyFrom(signature.toByteArray()));
    PBFTMessage message = builder.build();
    pbftMessage.setType(MessageTypes.PBFT_MSG.asByte())
        .setPbftMessage(message).setData(message.toByteArray()).setSwitch(block.isSwitch());
    return pbftMessage;
  }

  private static PbftMessage buildFullNodeCommon(DataType dataType, ByteString data,
      BlockCapsule block, long epoch, long viewN) {
    PbftMessage pbftMessage = new PbftMessage();
    Raw.Builder rawBuilder = Raw.newBuilder();
    PBFTMessage.Builder builder = PBFTMessage.newBuilder();
    rawBuilder.setViewN(viewN).setEpoch(epoch).setDataType(dataType)
        .setMsgType(MsgType.PREPREPARE).setData(data);
    Raw raw = rawBuilder.build();
    builder.setRawData(raw);
    PBFTMessage message = builder.build();
    pbftMessage.setType(MessageTypes.PBFT_MSG.asByte())
        .setPbftMessage(message).setData(message.toByteArray()).setSwitch(block.isSwitch());
    return pbftMessage;
  }

  public PbftMessage buildPrePareMessage(Miner miner) {
    return buildMessageCapsule(MsgType.PREPARE, miner);
  }

  public PbftMessage buildCommitMessage(Miner miner) {
    return buildMessageCapsule(MsgType.COMMIT, miner);
  }

  private PbftMessage buildMessageCapsule(MsgType type, Miner miner) {
    PbftMessage pbftMessage = new PbftMessage();
    ECKey ecKey = ECKey.fromPrivate(miner.getPrivateKey());
    PBFTMessage.Builder builder = PBFTMessage.newBuilder();
    Raw.Builder rawBuilder = Raw.newBuilder();
    rawBuilder.setViewN(getPbftMessage().getRawData().getViewN())
        .setDataType(getPbftMessage().getRawData().getDataType())
        .setMsgType(type).setEpoch(getPbftMessage().getRawData().getEpoch())
        .setData(getPbftMessage().getRawData().getData());
    Raw raw = rawBuilder.build();
    byte[] hash = Sha256Hash.hash(true, raw.toByteArray());
    ECDSASignature signature = ecKey.sign(hash);
    builder.setRawData(raw).setSignature(ByteString.copyFrom(signature.toByteArray()));
    PBFTMessage message = builder.build();
    pbftMessage.setType(getType().asByte())
        .setPbftMessage(message).setData(message.toByteArray());
    return pbftMessage;
  }
}