package br.usuario.clinica.dao;

import java.util.List;

import br.usuario.clinica.model.Doctor;

public interface DoctorDAO {
    Doctor addDoctor(String name, String specialty);
    Doctor findById(int id);
    List<Doctor> listAll();
    Doctor updateDoctor(int id, String name, String specialty);
}
