package org.kcoin.core.services.http;

import com.alibaba.fastjson.JSONObject;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.kcoin.core.capsule.TransactionCapsule;
import org.kcoin.core.utils.TransactionUtil;
import org.kcoin.protos.Protocol.Transaction;
import org.kcoin.protos.Protocol.TransactionSign;


@Component
@Slf4j(topic = "API")
public class AddTransactionSignServlet extends RateLimiterServlet {

  @Autowired
  private TransactionUtil transactionUtil;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {

  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String contract = request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()));
      Util.checkBodySize(contract);
      JSONObject input = JSONObject.parseObject(contract);
      boolean visible = Util.getVisibleOnlyForSign(input);
      String strTransaction = input.getJSONObject("transaction").toJSONString();
      Transaction transaction = Util.packTransaction(strTransaction, visible);
      JSONObject jsonTransaction = JSONObject.parseObject(JsonFormat.printToString(transaction,
          visible));
      input.put("transaction", jsonTransaction);
      TransactionSign.Builder build = TransactionSign.newBuilder();
      JsonFormat.merge(input.toJSONString(), build, visible);
      TransactionCapsule reply = transactionUtil.addSign(build.build());
      if (reply != null) {
        response.getWriter().println(Util.printCreateTransaction(reply.getInstance(), visible));
      } else {
        response.getWriter().println("{}");
      }
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }
}
