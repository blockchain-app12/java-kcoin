package org.kcoin.core.exception;

public class KcoinException extends Exception {

  public KcoinException() {
    super();
  }

  public KcoinException(String message) {
    super(message);
  }

  public KcoinException(String message, Throwable cause) {
    super(message, cause);
  }

}
