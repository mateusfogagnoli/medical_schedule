package br.usuario.clinica.dao;

import java.util.List;

import br.usuario.clinica.model.Patient;

public interface PatientDAO {
    Patient addPatient(String name, String phone);
    Patient findById(int id);
    List<Patient> listAll();
    Patient updatePatient(int id, String name, String phone);
}
