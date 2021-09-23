package org.kcoin.common.runtime;

import org.kcoin.core.db.TransactionContext;
import org.kcoin.core.exception.ContractExeException;
import org.kcoin.core.exception.ContractValidateException;


public interface Runtime {

  void execute(TransactionContext context)
      throws ContractValidateException, ContractExeException;

  ProgramResult getResult();

  String getRuntimeError();

}
