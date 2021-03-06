package org.kcoin.core.vm.nativecontract;

import static org.kcoin.core.actuator.ActuatorConstant.NOT_EXIST_STR;
import static org.kcoin.core.actuator.ActuatorConstant.WITNESS_EXCEPTION_STR;
import static org.kcoin.core.config.Parameter.ChainConstant.MAX_VOTE_NUMBER;
import static org.kcoin.core.config.Parameter.ChainConstant.SYM_PRECISION;

import com.google.common.math.LongMath;
import com.google.protobuf.ByteString;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.kcoin.common.utils.StringUtil;
import static org.kcoin.core.actuator.ActuatorConstant.*;
import org.kcoin.core.capsule.AccountCapsule;
import org.kcoin.core.capsule.VotesCapsule;
import org.kcoin.core.exception.ContractExeException;
import org.kcoin.core.exception.ContractValidateException;
import org.kcoin.core.vm.nativecontract.param.VoteWitnessParam;
import org.kcoin.core.vm.repository.Repository;
import org.kcoin.core.vm.utils.VoteRewardUtil;
import org.kcoin.protos.Protocol;

@Slf4j(topic = "Processor")
public class VoteWitnessProcessor {

  public void validate(VoteWitnessParam param, Repository repo) throws ContractValidateException {
    if (repo == null) {
      throw new ContractValidateException(STORE_NOT_EXIST);
    }

    if (param.getVotes().size() > MAX_VOTE_NUMBER) {
      throw new ContractValidateException(
          "VoteNumber more than maxVoteNumber " + MAX_VOTE_NUMBER);
    }
  }

  public void execute(VoteWitnessParam param, Repository repo) throws ContractExeException {
    byte[] ownerAddress = param.getVoterAddress();
    VoteRewardUtil.withdrawReward(ownerAddress, repo);

    AccountCapsule accountCapsule = repo.getAccount(ownerAddress);

    VotesCapsule votesCapsule = repo.getVotes(ownerAddress);
    if (votesCapsule == null) {
      votesCapsule = new VotesCapsule(ByteString.copyFrom(ownerAddress),
          accountCapsule.getVotesList());
    }

    accountCapsule.clearVotes();
    votesCapsule.clearNewVotes();

    Map<ByteString, Long> voteMap = new HashMap<>();
    Iterator<Protocol.Vote> iterator = param.getVotes().iterator();
    try {
      long sum = 0;
      while (iterator.hasNext()) {
        Protocol.Vote vote = iterator.next();

        byte[] witnessAddress = vote.getVoteAddress().toByteArray();
        /*
          Already covered while doing maintenance in MaintenanceManager.java, for tvm performance,
          we remove the account check
         */
//        if (repo.getAccount(witnessAddress) == null) {
//          throw new ContractValidateException(
//              ACCOUNT_EXCEPTION_STR + StringUtil.encode58Check(witnessAddress) + NOT_EXIST_STR);
//        }
        if (repo.getWitness(witnessAddress) == null) {
          throw new ContractExeException(
              WITNESS_EXCEPTION_STR + StringUtil.encode58Check(witnessAddress) + NOT_EXIST_STR);
        }

        long voteCount = vote.getVoteCount();
        if (voteCount < 0) {
          throw new ContractExeException("Vote count must not be less than 0");
        } else if (voteCount == 0) {
          iterator.remove();
        } else {
          sum = LongMath.checkedAdd(sum, voteCount);
          // merge vote for same witness
          voteMap.put(vote.getVoteAddress(),
              LongMath.checkedAdd(voteMap.getOrDefault(vote.getVoteAddress(), 0L), voteCount));
        }
      }

      long kcoinPower = accountCapsule.getKcoinPower();
      sum =  LongMath.checkedMultiply(sum, SYM_PRECISION);
      if (sum > kcoinPower) {
        throw new ContractExeException(
            "The total number of votes[" + sum + "] is greater than the kcoinPower[" + kcoinPower
                + "]");
      }
    } catch (ArithmeticException e) {
      throw new ContractExeException(e.getMessage());
    }

    for (Map.Entry<ByteString, Long> entry : voteMap.entrySet()) {
      accountCapsule.addVotes(entry.getKey(), entry.getValue());
      votesCapsule.addNewVotes(entry.getKey(), entry.getValue());
    }
    repo.updateAccount(accountCapsule.createDbKey(), accountCapsule);
    repo.updateVotes(ownerAddress, votesCapsule);
  }
}
