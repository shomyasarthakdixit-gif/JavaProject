package javaproject;

import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        // 🔹 Load rates from config file
        Map<Ward, Double> rates = RateConfigLoader.loadRates("rates.cfg");

        // Fallback if file missing or empty
        if (rates.isEmpty()) {
            rates.put(Ward.GENERAL, 1000.0);
            rates.put(Ward.ICU, 5000.0);
            rates.put(Ward.EMERGENCY, 3000.0);
        }

        HospitalManager manager =
                new HospitalManager(new StandardBilling(rates));

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n1.Admit \n2.Discharge \n3.List \n4.Analytics \n5.Exit");
            int ch = sc.nextInt();

            try {
                switch (ch) {
                case 1 -> {
                    System.out.print("Enter ID: ");
                    int id = sc.nextInt();

                    System.out.print("Enter Name: ");
                    String name = sc.next();

                    System.out.print("Enter Age: ");
                    int age = sc.nextInt();

                    System.out.print("Enter Number of Days: ");
                    int days = sc.nextInt();

                    System.out.println("Select Ward:");
                    System.out.println("GENERAL | ICU | EMERGENCY | PRIVATE");
                    System.out.print("Enter Ward: ");

                    Ward ward = Ward.valueOf(sc.next().toUpperCase());

                    manager.admit(new Patient(id, name, age, days, ward));
                }

                    case 2 -> {
                        System.out.print("Enter Patient ID: ");
                        manager.discharge(sc.nextInt());
                    }
                    case 3 -> manager.listPatients();
                    case 4 -> manager.analytics();
                    case 5 -> {
                        manager.savePatientsToCSV("patients.csv");
                        System.out.println("👋 Data saved. Exiting...");
                        System.exit(0);
                    }
                }
            } catch (Exception e) {
                System.out.println("❌ " + e.getMessage());
            }
        }
    }
}