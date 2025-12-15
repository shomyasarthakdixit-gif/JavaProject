package javaproject;

public class Patient extends Person {
    private int id;
    private int days;
    private boolean admitted;
    private Ward ward;

    public Patient(int id, String name, int age, int days, Ward ward) {
        super(name, age);
        this.id = id;
        this.days = days;
        this.ward = ward;
        this.admitted = true;
    }

    public int getId() { return id; }
    public int getDays() { return days; }
    public Ward getWard() { return ward; }
    public boolean isAdmitted() { return admitted; }

    public void discharge() {
        this.admitted = false;
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + age + " | " + ward + " | Days: " + days;
    }
}