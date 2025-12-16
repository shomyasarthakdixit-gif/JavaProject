package javaproject;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class HospitalManager implements BedObserver {

    private Map<Ward, List<Patient>> wardMap = new HashMap<>();
    private Map<Integer, Patient> roomOccupancy = new HashMap<>();
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
            System.out.println("⚠ patients.csv not found. Starting with empty records.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line = br.readLine(); // skip header

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String[] d = line.split(",");

                int id = Integer.parseInt(d[0].trim());
                String name = d[1].trim();
                int age = Integer.parseInt(d[2].trim());
                int days = Integer.parseInt(d[3].trim());
                Ward ward = Ward.valueOf(d[4].trim());
                int room = Integer.parseInt(d[5].trim());
                boolean admitted = Boolean.parseBoolean(d[6].trim());

                Patient p = new Patient(id, name, age, days, ward, room);

                if (!admitted) {
                    p.discharge();
                }

                // ⚠️ Only admit if record says admitted=true
                if (p.isAdmitted()) {
                    admit(p);
                }
            }

            System.out.println("✅ Patients loaded from CSV successfully.");

        } catch (Exception e) {
            System.out.println("❌ CSV Load Error: " + e.getMessage());
        }
    }

    
    // ==============================================

    public void admit(Patient patient)
            throws BedUnavailableException, RoomOccupiedException {

        int room = patient.getRoomNumber();

        if (roomOccupancy.containsKey(room)) {
            Patient existing = roomOccupancy.get(room);

            if (existing.isAdmitted()) {
                throw new RoomOccupiedException(
                    "Room " + room + " is already occupied by Patient ID: " + existing.getId()
                );
            }
        }

        bedManager.occupyBed();
        wardMap.get(patient.getWard()).add(patient);
        roomOccupancy.put(room, patient);

        System.out.println("✅ Patient admitted to Room " + room);
    }


    public void discharge(int id) throws InvalidPatientStateException {

        for (List<Patient> list : wardMap.values()) {
            for (Patient p : list) {

                if (p.getId() == id) {

                    if (!p.isAdmitted()) {
                        throw new InvalidPatientStateException("Patient already discharged");
                    }

                    // Mark discharged
                    p.discharge();

                    // Free room
                    roomOccupancy.remove(p.getRoomNumber());

                    // Release bed
                    bedManager.releaseBed();

                    // Generate bill
                    generateBill(p);

                    // Persist update
                    savePatientsToCSV("patients.csv");

                    System.out.println("✅ Patient discharged & room freed");
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

            pw.println("id,name,age,days,ward,room,admitted");

            for (List<Patient> list : wardMap.values()) {
                for (Patient p : list) {

                    pw.println(
                            p.getId() + "," +
                            p.name + "," +
                            p.age + "," +
                            p.getDays() + "," +
                            p.getWard() + "," +
                            p.getRoomNumber() + "," +
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