package org.kcoin.core.services.http;

import com.alibaba.fastjson.JSONObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.kcoin.core.Wallet;
import org.kcoin.protos.Protocol.Transaction;
import org.kcoin.protos.Protocol.Transaction.Contract.ContractType;
import org.kcoin.protos.contract.MarketContract.MarketCancelOrderContract;


@Component
@Slf4j(topic = "API")
public class MarketCancelOrderServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      PostParams params = PostParams.getPostParams(request);
      String contract = params.getParams();
      boolean visible = params.isVisible();
      MarketCancelOrderContract.Builder build = MarketCancelOrderContract.newBuilder();
      JsonFormat.merge(contract, build, visible);

      Transaction tx = wallet
          .createTransactionCapsule(build.build(), ContractType.MarketCancelOrderContract)
          .getInstance();

      JSONObject jsonObject = JSONObject.parseObject(contract);
      tx = Util.setTransactionPermissionId(jsonObject, tx);
      tx = Util.setTransactionExtraData(jsonObject, tx, visible);

      response.getWriter().println(Util.printCreateTransaction(tx, visible));
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }
}
