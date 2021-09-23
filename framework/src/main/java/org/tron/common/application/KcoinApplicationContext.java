package org.kcoin.common.application;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.kcoin.common.overlay.discover.DiscoverServer;
import org.kcoin.common.overlay.discover.node.NodeManager;
import org.kcoin.common.overlay.server.ChannelManager;
import org.kcoin.core.db.Manager;

public class KcoinApplicationContext extends AnnotationConfigApplicationContext {

  public KcoinApplicationContext() {
  }

  public KcoinApplicationContext(DefaultListableBeanFactory beanFactory) {
    super(beanFactory);
  }

  public KcoinApplicationContext(Class<?>... annotatedClasses) {
    super(annotatedClasses);
  }

  public KcoinApplicationContext(String... basePackages) {
    super(basePackages);
  }

  @Override
  public void destroy() {

    Application appT = ApplicationFactory.create(this);
    appT.shutdownServices();
    appT.shutdown();

    DiscoverServer discoverServer = getBean(DiscoverServer.class);
    discoverServer.close();
    ChannelManager channelManager = getBean(ChannelManager.class);
    channelManager.close();
    NodeManager nodeManager = getBean(NodeManager.class);
    nodeManager.close();

    Manager dbManager = getBean(Manager.class);
    dbManager.stopRePushThread();
    dbManager.stopRePushTriggerThread();
    super.destroy();
  }
}
