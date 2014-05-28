package org.graphwalker.core.machine;

import org.graphwalker.core.event.Observer;
import org.graphwalker.core.model.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nils Olsson
 */
public abstract class ObservableMachine implements Machine {

    private boolean changed = false;
    private List<Observer<Element>> observers = new ArrayList<>();

    public synchronized void addObserver(Observer<Element> observer) {
        if (observer == null)
            throw new NullPointerException();
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public synchronized void deleteObserver(Observer observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        notifyObservers(null);
    }

    public void notifyObservers(Element object) {
        List<Observer<Element>> observers;

        synchronized (this) {
            if (!changed) {
                return;
            }
            observers = new ArrayList<>(this.observers);
            clearChanged();
        }

        for (Observer<Element> observer: observers) {
            observer.update(this, object);
        }
    }

    public synchronized void deleteObservers() {
        observers.clear();
    }

    protected synchronized void setChanged() {
        changed = true;
    }

    protected synchronized void clearChanged() {
        changed = false;
    }

    public synchronized boolean hasChanged() {
        return changed;
    }

    public synchronized int countObservers() {
        return observers.size();
    }
}
