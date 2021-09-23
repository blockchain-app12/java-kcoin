package org.kcoin.common.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.kcoin.common.logsfilter.EventPluginLoader;
import org.kcoin.common.parameter.CommonParameter;
import org.kcoin.core.ChainBaseManager;
import org.kcoin.core.config.args.Args;
import org.kcoin.core.consensus.ConsensusService;
import org.kcoin.core.db.Manager;
import org.kcoin.core.metrics.MetricsUtil;
import org.kcoin.core.net.KcoinNetService;

@Slf4j(topic = "app")
@Component
public class ApplicationImpl implements Application {

  private ServiceContainer services;

  @Autowired
  private KcoinNetService kcoinNetService;

  @Autowired
  private Manager dbManager;

  @Autowired
  private ChainBaseManager chainBaseManager;

  @Autowired
  private ConsensusService consensusService;

  @Override
  public void setOptions(Args args) {
    // not used
  }

  @Override
  @Autowired
  public void init(CommonParameter parameter) {
    services = new ServiceContainer();
  }

  @Override
  public void addService(Service service) {
    services.add(service);
  }

  @Override
  public void initServices(CommonParameter parameter) {
    services.init(parameter);
  }

  /**
   * start up the app.
   */
  public void startup() {
    kcoinNetService.start();
    consensusService.start();
    MetricsUtil.init();
  }

  @Override
  public void shutdown() {
    logger.info("******** start to shutdown ********");
    kcoinNetService.stop();
    consensusService.stop();
    synchronized (dbManager.getRevokingStore()) {
      closeRevokingStore();
      closeAllStore();
    }
    dbManager.stopRePushThread();
    dbManager.stopRePushTriggerThread();
    EventPluginLoader.getInstance().stopPlugin();
    logger.info("******** end to shutdown ********");
  }

  @Override
  public void startServices() {
    services.start();
  }

  @Override
  public void shutdownServices() {
    services.stop();
  }

  @Override
  public Manager getDbManager() {
    return dbManager;
  }

  @Override
  public ChainBaseManager getChainBaseManager() {
    return chainBaseManager;
  }

  private void closeRevokingStore() {
    logger.info("******** start to closeRevokingStore ********");
    dbManager.getRevokingStore().shutdown();
  }

  private void closeAllStore() {
    dbManager.closeAllStore();
  }

}
