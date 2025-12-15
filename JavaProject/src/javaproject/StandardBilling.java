package javaproject;

public class StandardBilling extends BillingService {

    public StandardBilling(java.util.Map<Ward, Double> rates) {
        super(rates);
    }

    @Override
    public double calculateBill(Patient patient) {
        return patient.getDays() * rates.get(patient.getWard());
    }
}