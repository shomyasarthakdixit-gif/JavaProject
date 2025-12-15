package javaproject;

import java.util.ArrayList;
import java.util.List;

public class BedManager {
    private int availableBeds;
    private List<BedObserver> observers = new ArrayList<>();

    public BedManager(int beds) {
        this.availableBeds = beds;
    }

    public void addObserver(BedObserver obs) {
        observers.add(obs);
    }

    public void occupyBed() throws BedUnavailableException {
        if (availableBeds <= 0)
            throw new BedUnavailableException("No beds available!");
        availableBeds--;
        notifyObservers();
    }

    public void releaseBed() {
        availableBeds++;
    }

    private void notifyObservers() {
        if (availableBeds <= 2) {
            observers.forEach(o -> o.alert("⚠ Beds running low: " + availableBeds));
        }
    }
}