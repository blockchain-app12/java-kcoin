package org.kcoin.core.exception;

public class DupTransactionException extends KcoinException {

  public DupTransactionException() {
    super();
  }

  public DupTransactionException(String message) {
    super(message);
  }
}
