package jre;

import java.util.concurrent.atomic.AtomicInteger;

class State {

  private static final AtomicInteger counter = new AtomicInteger();

  private final int id = counter.incrementAndGet();

  @Override
  public String toString() {
    return String.valueOf(id);
  }
}
