package br.usuario.clinica.dao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import br.usuario.clinica.model.Appointment;

public class InMemoryAppointmentDAO implements AppointmentDAO {
    private final Map<Integer, Appointment> map = new HashMap<>();
    private final AtomicInteger seq = new AtomicInteger(1);

    @Override public Appointment addAppointment(Appointment appt) { int id = seq.getAndIncrement(); Appointment stored = new Appointment(id, appt.getPatientId(), appt.getDoctorId(), appt.getStart(), appt.getEnd()); map.put(id, stored); return stored; }
    @Override public boolean removeAppointment(int id) { return map.remove(id) != null; }
    @Override public Appointment findById(int id) { return map.get(id); }
    @Override public List<Appointment> listAll() { List<Appointment> list = new ArrayList<>(map.values()); list.sort(Comparator.comparing(Appointment::getStart)); return list; }
    @Override public boolean updateAppointment(Appointment appt) { if (!map.containsKey(appt.getId())) return false; map.put(appt.getId(), appt); return true; }
}
