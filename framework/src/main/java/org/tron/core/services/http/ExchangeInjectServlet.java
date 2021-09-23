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
import org.kcoin.protos.contract.ExchangeContract.ExchangeInjectContract;


@Component
@Slf4j(topic = "API")
public class ExchangeInjectServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      PostParams params = PostParams.getPostParams(request);
      ExchangeInjectContract.Builder build = ExchangeInjectContract.newBuilder();
      JsonFormat.merge(params.getParams(), build, params.isVisible());
      Transaction tx = wallet
          .createTransactionCapsule(build.build(), ContractType.ExchangeInjectContract)
          .getInstance();
      JSONObject jsonObject = JSONObject.parseObject(params.getParams());
      tx = Util.setTransactionPermissionId(jsonObject, tx);
      response.getWriter().println(Util
          .printCreateTransaction(tx, params.isVisible()));
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }
}
