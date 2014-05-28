package org.graphwalker.core.event;

/**
 * @author Nils Olsson
 */
public interface Observer<T> {
    void update(Observable<T> observable, T object);
}
