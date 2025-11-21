package br.usuario.clinica.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import br.usuario.clinica.model.Patient;

public class InMemoryPatientDAO implements PatientDAO {
    private final Map<Integer, Patient> map = new HashMap<>();
    private final AtomicInteger seq = new AtomicInteger(1);

    @Override public Patient addPatient(String name, String phone) { int id = seq.getAndIncrement(); Patient p = new Patient(id, name, phone); map.put(id, p); return p; }
    @Override public Patient findById(int id) { return map.get(id); }
    @Override public List<Patient> listAll() { return new ArrayList<>(map.values()); }
    @Override public Patient updatePatient(int id, String name, String phone) { Patient existing = map.get(id); if (existing == null) return null; Patient updated = new Patient(id, name, phone); map.put(id, updated); return updated; }
}
