package org.kcoin.core.exception;

public class UnReachBlockException extends KcoinException {

  public UnReachBlockException() {
    super();
  }

  public UnReachBlockException(String message) {
    super(message);
  }

  public UnReachBlockException(String message, Throwable cause) {
    super(message, cause);
  }
}
