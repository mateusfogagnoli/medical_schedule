package br.usuario.clinica.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.usuario.clinica.model.Appointment;
import br.usuario.clinica.util.DBConnection;

public class JdbcAppointmentDAO implements AppointmentDAO {
    @Override
    public Appointment addAppointment(Appointment appt) {
        String sql = "INSERT INTO appointments(patient_id,doctor_id,start,end) VALUES(?,?,?,?)";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, appt.getPatientId());
            ps.setInt(2, appt.getDoctorId());
            ps.setString(3, appt.getStart().toString());
            ps.setString(4, appt.getEnd().toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return new Appointment(rs.getInt(1), appt.getPatientId(), appt.getDoctorId(), appt.getStart(), appt.getEnd()); }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return null;
    }

    @Override
    public boolean removeAppointment(int id) {
        String sql = "DELETE FROM appointments WHERE id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Appointment findById(int id) {
        String sql = "SELECT id,patient_id,doctor_id,start,end FROM appointments WHERE id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return null;
    }

    @Override
    public List<Appointment> listAll() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT id,patient_id,doctor_id,start,end FROM appointments ORDER BY id";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    @Override
    public boolean updateAppointment(Appointment appt) {
        String sql = "UPDATE appointments SET patient_id=?, doctor_id=?, start=?, end=? WHERE id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, appt.getPatientId());
            ps.setInt(2, appt.getDoctorId());
            ps.setString(3, appt.getStart().toString());
            ps.setString(4, appt.getEnd().toString());
            ps.setInt(5, appt.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private Appointment map(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int patientId = rs.getInt("patient_id");
        int doctorId = rs.getInt("doctor_id");
        LocalDateTime start = LocalDateTime.parse(rs.getString("start"));
        LocalDateTime end = LocalDateTime.parse(rs.getString("end"));
        return new Appointment(id, patientId, doctorId, start, end);
    }
}
