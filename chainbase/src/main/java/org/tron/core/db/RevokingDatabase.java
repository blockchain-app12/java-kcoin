package org.kcoin.core.db;

import org.kcoin.core.db2.ISession;
import org.kcoin.core.db2.common.IRevokingDB;
import org.kcoin.core.db2.core.Chainbase;
import org.kcoin.core.exception.RevokingStoreIllegalStateException;

public interface RevokingDatabase {

  ISession buildSession();

  ISession buildSession(boolean forceEnable);

  void setCursor(Chainbase.Cursor cursor);

  void setCursor(Chainbase.Cursor cursor, long offset);

  void add(IRevokingDB revokingDB);

  void merge() throws RevokingStoreIllegalStateException;

  void revoke() throws RevokingStoreIllegalStateException;

  void commit() throws RevokingStoreIllegalStateException;

  void pop() throws RevokingStoreIllegalStateException;

  void fastPop() throws RevokingStoreIllegalStateException;

  void enable();

  int size();

  void check();

  void setMaxSize(int maxSize);

  void disable();

  void setMaxFlushCount(int maxFlushCount);

  void shutdown();

}
