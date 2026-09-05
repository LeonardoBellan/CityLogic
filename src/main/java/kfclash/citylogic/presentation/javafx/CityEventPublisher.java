package kfclash.citylogic.presentation.javafx;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import kfclash.citylogic.domain.core.CitySnapshot;
import kfclash.citylogic.ports.ICityEventPublisher;
import kfclash.citylogic.ports.ICityObserver;

public final class CityEventPublisher implements ICityEventPublisher {
    private final List<ICityObserver> observers = new CopyOnWriteArrayList<>();

    @Override
    public void publish(CitySnapshot snapshot) {
        for (ICityObserver observer : observers) {
            observer.onMetricsChanged(snapshot);
        }
    }

    @Override
    public void subscribe(ICityObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("observer cannot be null");
        }
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void unsubscribe(ICityObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("observer cannot be null");
        }
        observers.remove(observer);
    }
}
