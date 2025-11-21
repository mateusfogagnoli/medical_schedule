package br.usuario.clinica.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import br.usuario.clinica.model.Doctor;

public class InMemoryDoctorDAO implements DoctorDAO {
    private final Map<Integer, Doctor> map = new HashMap<>();
    private final AtomicInteger seq = new AtomicInteger(1);

    @Override public Doctor addDoctor(String name, String specialty) { int id = seq.getAndIncrement(); Doctor d = new Doctor(id, name, specialty); map.put(id, d); return d; }
    @Override public Doctor findById(int id) { return map.get(id); }
    @Override public List<Doctor> listAll() { return new ArrayList<>(map.values()); }
    @Override public Doctor updateDoctor(int id, String name, String specialty) { Doctor existing = map.get(id); if (existing == null) return null; Doctor updated = new Doctor(id, name, specialty); map.put(id, updated); return updated; }
}
