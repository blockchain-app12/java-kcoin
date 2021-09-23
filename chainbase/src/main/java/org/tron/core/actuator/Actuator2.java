package org.kcoin.core.actuator;

import org.kcoin.core.exception.ContractExeException;
import org.kcoin.core.exception.ContractValidateException;

public interface Actuator2 {

  void execute(Object object) throws ContractExeException;

  void validate(Object object) throws ContractValidateException;
}