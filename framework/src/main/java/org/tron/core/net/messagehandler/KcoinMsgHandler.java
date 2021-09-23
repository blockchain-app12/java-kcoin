package org.kcoin.core.net.messagehandler;

import org.kcoin.core.exception.P2pException;
import org.kcoin.core.net.message.KcoinMessage;
import org.kcoin.core.net.peer.PeerConnection;

public interface KcoinMsgHandler {

  void processMessage(PeerConnection peer, KcoinMessage msg) throws P2pException;

}
