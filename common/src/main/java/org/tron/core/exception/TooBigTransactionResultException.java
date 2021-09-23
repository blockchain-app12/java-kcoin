package org.kcoin.core.exception;

public class TooBigTransactionResultException extends KcoinException {

  public TooBigTransactionResultException() {
    super("too big transaction result");
  }

  public TooBigTransactionResultException(String message) {
    super(message);
  }
}
