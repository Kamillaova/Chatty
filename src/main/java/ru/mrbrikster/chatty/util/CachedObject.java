package ru.mrbrikster.chatty.util;

import java.util.function.Supplier;

public class CachedObject<K, V> {
  private K key;
  private V value;

  public CachedObject(K key, V value) {
    this.key = key;
    this.value = value;
  }

  public V get(K key, Supplier<V> supplier) {
    if (this.key.equals(key)) {
      return value;
    }

    this.key = key;
    this.value = supplier.get();
    return value;
  }
}
