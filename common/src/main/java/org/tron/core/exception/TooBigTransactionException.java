package org.kcoin.core.exception;

public class TooBigTransactionException extends KcoinException {

  public TooBigTransactionException() {
    super();
  }

  public TooBigTransactionException(String message) {
    super(message);
  }
}
