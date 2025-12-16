package javaproject;

public class Patient extends Person {
    private int id;
    private int days;
    private boolean admitted;
    private Ward ward;
    private int roomNumber;
    
    public Patient(int id, String name, int age, int days, Ward ward, int roomNumber) {
        super(name, age);
        this.id = id;
        this.days = days;
        this.ward = ward;
        this.roomNumber = roomNumber;
        this.admitted = true;
    }

    public int getId() { return id; }
    public int getDays() { return days; }
    public Ward getWard() { return ward; }
    public int getRoomNumber() { return roomNumber; }
    public boolean isAdmitted() { return admitted; }

    public void discharge() {
        this.admitted = false;
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + age + " | " + ward + " | Days: " + days;
    }
    

}