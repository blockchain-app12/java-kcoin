package org.kcoin.core.exception;

public class KcoinRuntimeException extends RuntimeException {

  public KcoinRuntimeException() {
    super();
  }

  public KcoinRuntimeException(String message) {
    super(message);
  }

  public KcoinRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public KcoinRuntimeException(Throwable cause) {
    super(cause);
  }

  protected KcoinRuntimeException(String message, Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }


}
