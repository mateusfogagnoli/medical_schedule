package br.usuario.clinica.model;

import java.time.LocalDateTime;

public class Appointment {
    private final int id;
    private final int patientId;
    private final int doctorId;
    private final LocalDateTime start;
    private final LocalDateTime end;
    public Appointment(int id, int patientId, int doctorId, LocalDateTime start, LocalDateTime end) { this.id = id; this.patientId = patientId; this.doctorId = doctorId; this.start = start; this.end = end; }
    public int getId() { return id; }
    public int getPatientId() { return patientId; }
    public int getDoctorId() { return doctorId; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public boolean overlaps(Appointment other) { return start.isBefore(other.end) && other.start.isBefore(end); }
    @Override public String toString() { return id + ", patient=" + patientId + ", doctor=" + doctorId + ", " + start + " - " + end; }
}
