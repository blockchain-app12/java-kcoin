package org.kcoin.core.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.kcoin.common.overlay.server.Channel;
import org.kcoin.common.overlay.server.MessageQueue;
import org.kcoin.core.net.message.KcoinMessage;
import org.kcoin.core.net.peer.PeerConnection;

@Component
@Scope("prototype")
public class KcoinNetHandler extends SimpleChannelInboundHandler<KcoinMessage> {

  protected PeerConnection peer;

  private MessageQueue msgQueue;

  @Autowired
  private KcoinNetService kcoinNetService;

  @Override
  public void channelRead0(final ChannelHandlerContext ctx, KcoinMessage msg) throws Exception {
    msgQueue.receivedMessage(msg);
    kcoinNetService.onMessage(peer, msg);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    peer.processException(cause);
  }

  public void setMsgQueue(MessageQueue msgQueue) {
    this.msgQueue = msgQueue;
  }

  public void setChannel(Channel channel) {
    this.peer = (PeerConnection) channel;
  }

}