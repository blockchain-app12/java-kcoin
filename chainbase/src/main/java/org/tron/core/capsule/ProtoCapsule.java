package org.kcoin.core.capsule;

public interface ProtoCapsule<T> {

  byte[] getData();

  T getInstance();
}
