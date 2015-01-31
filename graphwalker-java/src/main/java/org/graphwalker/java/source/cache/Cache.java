package org.graphwalker.java.source.cache;

/**
 * @author Nils Olsson
 */
public interface Cache<K,V> {

    void add(K key, V value);
    V get(K key);
    boolean contains(K key);
}
