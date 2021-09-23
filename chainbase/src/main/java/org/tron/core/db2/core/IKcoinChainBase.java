package org.kcoin.core.db2.core;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Map.Entry;
import org.kcoin.common.utils.Quitable;
import org.kcoin.core.exception.BadItemException;
import org.kcoin.core.exception.ItemNotFoundException;

public interface IKcoinChainBase<T> extends Iterable<Entry<byte[], T>>, Quitable {

  /**
   * reset the database.
   */
  void reset();

  /**
   * close the database.
   */
  void close();

  void put(byte[] key, T item);

  void delete(byte[] key);

  T get(byte[] key) throws InvalidProtocolBufferException, ItemNotFoundException, BadItemException;

  T getUnchecked(byte[] key);

  boolean has(byte[] key);

  boolean isNotEmpty();

  String getName();

  String getDbName();

}
