package org.kcoin.core.exception;

public class BalanceInsufficientException extends KcoinException {

  public BalanceInsufficientException() {
    super();
  }

  public BalanceInsufficientException(String message) {
    super(message);
  }
}
