package org.kcoin.core.services.http;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.kcoin.api.GrpcAPI;
import org.kcoin.core.Wallet;


@Component
@Slf4j(topic = "API")
public class GetDiversifierServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      PostParams params = PostParams.getPostParams(request);
      fillResponse(params.isVisible(), response);
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      boolean visible = Util.getVisible(request);
      fillResponse(visible, response);
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }

  private void fillResponse(boolean visible, HttpServletResponse response) throws Exception {

    GrpcAPI.DiversifierMessage d = wallet.getDiversifier();

    if (d != null) {
      response.getWriter().println(JsonFormat.printToString(d, visible));
    } else {
      response.getWriter().println("{}");
    }
  }
}
