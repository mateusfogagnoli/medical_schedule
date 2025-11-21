package br.usuario.clinica.dao;

import java.util.List;

import br.usuario.clinica.model.Appointment;

public interface AppointmentDAO {
    Appointment addAppointment(Appointment appt);
    boolean removeAppointment(int id);
    Appointment findById(int id);
    List<Appointment> listAll();
    boolean updateAppointment(Appointment appt);
}
