package org.kcoin.core.db;

import com.google.protobuf.ByteString;
import java.io.File;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kcoin.common.application.KcoinApplicationContext;
import org.kcoin.common.utils.ByteArray;
import org.kcoin.common.utils.FileUtil;
import org.kcoin.core.Constant;
import org.kcoin.core.capsule.AccountAssetCapsule;
import org.kcoin.core.config.DefaultConfig;
import org.kcoin.core.config.args.Args;
import org.kcoin.core.store.AccountAssetStore;


public class AccountAssetStoreTest {

  private static final byte[] data = TransactionStoreTest.randomBytes(32);
  private static String dbPath = "output_AccountAssetStore_test";
  private static String dbDirectory = "db_AccountAssetStore_test";
  private static String indexDirectory = "index_AccountAssetStore_test";
  private static KcoinApplicationContext context;
  private static AccountAssetStore accountAssetStore;
  private static byte[] address = TransactionStoreTest.randomBytes(32);

  static {
    Args.setParam(
        new String[]{
            "--output-directory", dbPath,
            "--storage-db-directory", dbDirectory,
            "--storage-index-directory", indexDirectory
        },
        Constant.TEST_CONF
    );
    context = new KcoinApplicationContext(DefaultConfig.class);
  }

  @AfterClass
  public static void destroy() {
    Args.clearParam();
    context.destroy();
    FileUtil.deleteDir(new File(dbPath));
  }

  @BeforeClass
  public static void init() {
    accountAssetStore = context.getBean(AccountAssetStore.class);
    AccountAssetCapsule accountCapsule = new AccountAssetCapsule(
            ByteString.copyFrom(address));
    accountAssetStore.put(data, accountCapsule);
  }

  @Test
  public void get() {
    Assert.assertEquals(ByteArray.toHexString(address),
            ByteArray.toHexString(accountAssetStore.get(data)
                            .getInstance().getAddress().toByteArray()));
    Assert.assertTrue(accountAssetStore.has(data));
  }

}
