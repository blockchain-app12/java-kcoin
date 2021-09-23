package org.kcoin.core.services.http;

import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.kcoin.api.GrpcAPI;
import org.kcoin.api.GrpcAPI.EasyTransferMessage;
import org.kcoin.api.GrpcAPI.EasyTransferResponse;
import org.kcoin.api.GrpcAPI.Return.response_code;
import org.kcoin.common.crypto.SignInterface;
import org.kcoin.common.crypto.SignUtils;
import org.kcoin.core.Wallet;
import org.kcoin.core.capsule.TransactionCapsule;
import org.kcoin.core.config.args.Args;
import org.kcoin.core.exception.ContractValidateException;
import org.kcoin.core.services.http.JsonFormat.ParseException;
import org.kcoin.protos.Protocol.Transaction.Contract.ContractType;
import org.kcoin.protos.contract.BalanceContract.TransferContract;


@Component
@Slf4j(topic = "API")
public class EasyTransferServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {

  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    GrpcAPI.Return.Builder returnBuilder = GrpcAPI.Return.newBuilder();
    EasyTransferResponse.Builder responseBuild = EasyTransferResponse.newBuilder();
    boolean visible = false;
    try {
      String input = request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()));
      Util.checkBodySize(input);
      visible = Util.getVisiblePost(input);
      EasyTransferMessage.Builder build = EasyTransferMessage.newBuilder();
      JsonFormat.merge(input, build, visible);
      byte[] privateKey = wallet.pass2Key(build.getPassPhrase().toByteArray());
      SignInterface ecKey = SignUtils.fromPrivate(privateKey, Args.getInstance()
          .isECKeyCryptoEngine());
      byte[] owner = ecKey.getAddress();
      TransferContract.Builder builder = TransferContract.newBuilder();
      builder.setOwnerAddress(ByteString.copyFrom(owner));
      builder.setToAddress(build.getToAddress());
      builder.setAmount(build.getAmount());

      TransactionCapsule transactionCapsule;
      transactionCapsule = wallet
          .createTransactionCapsule(builder.build(), ContractType.TransferContract);
      transactionCapsule.sign(privateKey);
      GrpcAPI.Return result = wallet.broadcastTransaction(transactionCapsule.getInstance());
      responseBuild.setTransaction(transactionCapsule.getInstance());
      responseBuild.setResult(result);
      response.getWriter().println(Util.printEasyTransferResponse(responseBuild.build(), visible));
    } catch (ParseException e) {
      logger.debug("ParseException: {}", e.getMessage());
    } catch (IOException e) {
      logger.debug("IOException: {}", e.getMessage());
    } catch (ContractValidateException e) {
      returnBuilder.setResult(false).setCode(response_code.CONTRACT_VALIDATE_ERROR)
          .setMessage(ByteString.copyFromUtf8(e.getMessage()));
      responseBuild.setResult(returnBuilder.build());
      try {
        response.getWriter().println(JsonFormat.printToString(responseBuild.build(), visible));
      } catch (IOException ioe) {
        logger.debug("IOException: {}", ioe.getMessage());
      }
      return;
    } catch (Exception e) {
      logger.debug("Exception: {}", e.getMessage());
    }
  }
}
