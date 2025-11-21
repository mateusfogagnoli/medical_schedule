package br.usuario.clinica.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.usuario.clinica.model.Patient;
import br.usuario.clinica.util.DBConnection;

public class JdbcPatientDAO implements PatientDAO {
    @Override
    public Patient addPatient(String name, String phone) {
        String sql = "INSERT INTO patients(name,phone) VALUES(?,?)";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name); ps.setString(2, phone); ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return new Patient(rs.getInt(1), name, phone); }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return null;
    }

    @Override
    public Patient findById(int id) {
        String sql = "SELECT id,name,phone FROM patients WHERE id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return new Patient(rs.getInt("id"), rs.getString("name"), rs.getString("phone")); }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return null;
    }

    @Override
    public List<Patient> listAll() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT id,name,phone FROM patients ORDER BY id";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new Patient(rs.getInt("id"), rs.getString("name"), rs.getString("phone")));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    @Override
    public Patient updatePatient(int id, String name, String phone) {
        String sql = "UPDATE patients SET name=?, phone=? WHERE id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, phone); ps.setInt(3, id); ps.executeUpdate();
            return new Patient(id, name, phone);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
