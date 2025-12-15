package javaproject;

public class InvalidPatientStateException extends Exception {
    public InvalidPatientStateException(String msg) {
        super(msg);
    }
}