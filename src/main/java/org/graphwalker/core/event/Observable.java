package org.graphwalker.core.event;

/**
 * @author Nils Olsson
 */
public interface Observable<T> {

    void addObserver(Observer<T> observer) ;
    void deleteObserver(Observer observer);
    void notifyObservers();
    void notifyObservers(T object);
    void deleteObservers();
    boolean hasChanged();
    int countObservers();
}
