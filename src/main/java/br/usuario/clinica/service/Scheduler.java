package br.usuario.clinica.service;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import br.usuario.clinica.dao.AppointmentDAO;
import br.usuario.clinica.dao.InMemoryAppointmentDAO;
import br.usuario.clinica.model.Appointment;

public class Scheduler {
    private final AppointmentDAO apptDao;
    private final Deque<WaitingEntry> waiting = new ArrayDeque<>();

    public Scheduler() { this.apptDao = new InMemoryAppointmentDAO(); }
    public Scheduler(AppointmentDAO apptDao) { this.apptDao = apptDao; }

    public static class WaitingEntry {
        public final int patientId;
        public final int doctorId;
        public final LocalDateTime start;
        public final LocalDateTime end;
        public WaitingEntry(int patientId, int doctorId, LocalDateTime start, LocalDateTime end) { this.patientId = patientId; this.doctorId = doctorId; this.start = start; this.end = end; }
    }

    public List<Appointment> listAppointments() { return apptDao.listAll(); }

    public Appointment schedule(int patientId, int doctorId, LocalDateTime start, LocalDateTime end) {
        Appointment candidate = new Appointment(-1, patientId, doctorId, start, end);
        if (hasConflict(candidate)) { waiting.addLast(new WaitingEntry(patientId, doctorId, start, end)); return null; }
        return apptDao.addAppointment(candidate);
    }

    public boolean canScheduleIgnoring(int ignoreId, Appointment candidate) {
        for (Appointment a : apptDao.listAll()) {
            if (a.getId() == ignoreId) continue;
            boolean sameDoctor = a.getDoctorId() == candidate.getDoctorId();
            boolean samePatient = a.getPatientId() == candidate.getPatientId();
            if ((sameDoctor || samePatient) && a.overlaps(candidate)) return false;
        }
        return true;
    }

    public boolean updateAppointment(Appointment appt) {
        boolean ok = apptDao.updateAppointment(appt);
        if (ok) processWaitingList();
        return ok;
    }

    private boolean hasConflict(Appointment candidate) {
        for (Appointment a : apptDao.listAll()) {
            boolean sameDoctor = a.getDoctorId() == candidate.getDoctorId();
            boolean samePatient = a.getPatientId() == candidate.getPatientId();
            if ((sameDoctor || samePatient) && a.overlaps(candidate)) return true;
        }
        return false;
    }

    public boolean cancelAppointment(int id) {
        boolean removed = apptDao.removeAppointment(id);
        if (removed) processWaitingList();
        return removed;
    }

    private void processWaitingList() {
        Iterator<WaitingEntry> it = waiting.iterator();
        List<WaitingEntry> scheduled = new ArrayList<>();
        while (it.hasNext()) {
            WaitingEntry we = it.next();
            Appointment candidate = new Appointment(-1, we.patientId, we.doctorId, we.start, we.end);
            if (!hasConflict(candidate)) { apptDao.addAppointment(candidate); scheduled.add(we); }
        }
        waiting.removeAll(scheduled);
    }

    public List<WaitingEntry> listWaiting() { return new ArrayList<>(waiting); }
}
