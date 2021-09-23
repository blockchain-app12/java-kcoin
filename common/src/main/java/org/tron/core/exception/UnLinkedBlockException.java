package org.kcoin.core.exception;

public class UnLinkedBlockException extends KcoinException {

  public UnLinkedBlockException() {
    super();
  }

  public UnLinkedBlockException(String message) {
    super(message);
  }

  public UnLinkedBlockException(String message, Throwable cause) {
    super(message, cause);
  }
}
