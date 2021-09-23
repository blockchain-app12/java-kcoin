package org.kcoin.core.db;

import com.google.protobuf.InvalidProtocolBufferException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map.Entry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.iq80.leveldb.WriteOptions;
import org.kcoin.common.parameter.CommonParameter;
import org.kcoin.common.storage.leveldb.LevelDbDataSourceImpl;
import org.kcoin.common.storage.rocksdb.RocksDbDataSourceImpl;
import org.kcoin.common.utils.StorageUtils;
import org.kcoin.core.db.common.DbSourceInter;
import org.kcoin.core.db2.core.IKcoinChainBase;
import org.kcoin.core.exception.BadItemException;
import org.kcoin.core.exception.ItemNotFoundException;

@Slf4j(topic = "DB")
public abstract class KcoinDatabase<T> implements IKcoinChainBase<T> {

  protected DbSourceInter<byte[]> dbSource;
  @Getter
  private String dbName;

  protected KcoinDatabase(String dbName) {
    this.dbName = dbName;

    if ("LEVELDB".equals(CommonParameter.getInstance().getStorage()
        .getDbEngine().toUpperCase())) {
      dbSource =
          new LevelDbDataSourceImpl(StorageUtils.getOutputDirectoryByDbName(dbName),
              dbName,
              StorageUtils.getOptionsByDbName(dbName),
              new WriteOptions().sync(CommonParameter.getInstance()
                  .getStorage().isDbSync()));
    } else if ("ROCKSDB".equals(CommonParameter.getInstance()
        .getStorage().getDbEngine().toUpperCase())) {
      String parentName = Paths.get(StorageUtils.getOutputDirectoryByDbName(dbName),
          CommonParameter.getInstance().getStorage().getDbDirectory()).toString();
      dbSource =
          new RocksDbDataSourceImpl(parentName, dbName, CommonParameter.getInstance()
              .getRocksDBCustomSettings());
    }

    dbSource.initDB();
  }

  protected KcoinDatabase() {
  }

  public DbSourceInter<byte[]> getDbSource() {
    return dbSource;
  }

  /**
   * reset the database.
   */
  public void reset() {
    dbSource.resetDb();
  }

  /**
   * close the database.
   */
  @Override
  public void close() {
    dbSource.closeDB();
  }

  public abstract void put(byte[] key, T item);

  public abstract void delete(byte[] key);

  public abstract T get(byte[] key)
      throws InvalidProtocolBufferException, ItemNotFoundException, BadItemException;

  public T getUnchecked(byte[] key) {
    return null;
  }

  public abstract boolean has(byte[] key);

  @Override
  public  boolean isNotEmpty() {
    throw new UnsupportedOperationException();
  }

  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public Iterator<Entry<byte[], T>> iterator() {
    throw new UnsupportedOperationException();
  }
}
