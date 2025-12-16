package javaproject;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class HospitalManager implements BedObserver {

    private Map<Ward, List<Patient>> wardMap = new HashMap<>();
    private BedManager bedManager = new BedManager(10);
    private BillingService billingService;

    public HospitalManager(BillingService billingService) {
        this.billingService = billingService;
        bedManager.addObserver(this);

        for (Ward w : Ward.values())
            wardMap.put(w, new ArrayList<>());

        loadPatientsFromCSV("patients.csv");  // MUST EXIST
    }
    
    private String clean(String value) {
        return value.replace("\"", "").trim();
    }

    // ================= CSV READER =================
    private void loadPatientsFromCSV(String fileName) {

        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println("⚠ patients.csv not found. Starting empty.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line = br.readLine(); // header

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String[] d = line.split(",");

                Patient p = new Patient(
                        Integer.parseInt(clean(d[0])),
                        clean(d[1]),
                        Integer.parseInt(clean(d[2])),
                        Integer.parseInt(clean(d[3])),
                        Ward.valueOf(clean(d[4]))
                );

                if (d.length == 6 && clean(d[5]).equalsIgnoreCase("false")) {
                    p.discharge();
                }

                admit(p);
            }

            System.out.println("✅ Patients loaded from CSV");

        } catch (Exception e) {
            System.out.println("❌ CSV Load Error: " + e.getMessage());
        }
    }
    
    // ==============================================

    public void admit(Patient p) throws BedUnavailableException {
        bedManager.occupyBed();
        wardMap.get(p.getWard()).add(p);
    }

    public void discharge(int id) throws InvalidPatientStateException {

        for (List<Patient> list : wardMap.values()) {
            for (Patient p : list) {

                if (p.getId() == id) {

                    if (!p.isAdmitted()) {
                        throw new InvalidPatientStateException("Patient already discharged");
                    }

                    // 1️⃣ Mark patient as discharged
                    p.discharge();

                    // 2️⃣ Release bed
                    bedManager.releaseBed();

                    // 3️⃣ Generate bill
                    generateBill(p);

                    // 4️⃣ Save updated records to CSV
                    savePatientsToCSV("patients.csv");

                    System.out.println("✅ Patient discharged (record retained)");
                    return;
                }
            }
        }

        throw new InvalidPatientStateException("Patient not found");
    }

    public void listPatients() {
        wardMap.values().forEach(list -> list.forEach(System.out::println));
    }

    public void analytics() {
        List<Patient> all = wardMap.values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        double avgStay = all.stream()
                .mapToInt(Patient::getDays)
                .average()
                .orElse(0);

        double occupancy = (all.size() / 10.0) * 100;

        System.out.println("📊 Avg Stay Length: " + avgStay);
        System.out.println("📊 Occupancy %: " + occupancy);
    }

    @Override
    public void alert(String message) {
        System.out.println(message);
    }
    public void savePatientsToCSV(String fileName) {

        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {

            pw.println("id,name,age,days,ward,admitted");

            for (List<Patient> list : wardMap.values()) {
                for (Patient p : list) {

                    pw.println(
                            p.getId() + "," +
                            p.name + "," +
                            p.age + "," +
                            p.getDays() + "," +
                            p.getWard() + "," +
                            p.isAdmitted()
                    );
                }
            }

        } catch (IOException e) {
            System.out.println("❌ CSV Save Error: " + e.getMessage());
        }
    }
    
//Bill
    
    private void generateBill(Patient patient) {

        double amount = billingService.calculateBill(patient);

        try (PrintWriter pw = new PrintWriter(new FileWriter("billing.txt", true))) {

            pw.println("Patient ID: " + patient.getId());
            pw.println("Name      : " + patient.name);
            pw.println("Ward      : " + patient.getWard());
            pw.println("Days Stay : " + patient.getDays());
            pw.println("Amount    : ₹" + amount);
            pw.println("--------------------------------");

        } catch (IOException e) {
            System.out.println("❌ Billing file error: " + e.getMessage());
        }
    }
}