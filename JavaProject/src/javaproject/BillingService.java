package javaproject;

import java.util.Map;

public abstract class BillingService implements Service {
    protected Map<Ward, Double> rates;

    public BillingService(Map<Ward, Double> rates) {
        this.rates = rates;
    }
}