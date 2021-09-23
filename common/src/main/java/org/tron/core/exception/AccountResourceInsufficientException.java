package org.kcoin.core.exception;

public class AccountResourceInsufficientException extends KcoinException {

  public AccountResourceInsufficientException() {
    super("Insufficient bandwidth and balance to create new account");
  }

  public AccountResourceInsufficientException(String message) {
    super(message);
  }
}

