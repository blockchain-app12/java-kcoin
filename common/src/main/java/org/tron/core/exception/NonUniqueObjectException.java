package org.kcoin.core.exception;

public class NonUniqueObjectException extends KcoinException {

  public NonUniqueObjectException() {
    super();
  }

  public NonUniqueObjectException(String s) {
    super(s);
  }

  public NonUniqueObjectException(String message, Throwable cause) {
    super(message, cause);
  }

  public NonUniqueObjectException(Throwable cause) {
    super("", cause);
  }
}
