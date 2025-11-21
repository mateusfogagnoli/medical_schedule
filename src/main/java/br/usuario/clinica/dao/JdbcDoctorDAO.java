package br.usuario.clinica.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.usuario.clinica.model.Doctor;
import br.usuario.clinica.util.DBConnection;

public class JdbcDoctorDAO implements DoctorDAO {
    @Override
    public Doctor addDoctor(String name, String specialty) {
        String sql = "INSERT INTO doctors(name,specialty) VALUES(?,?)";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name); ps.setString(2, specialty); ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return new Doctor(rs.getInt(1), name, specialty); }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return null;
    }

    @Override
    public Doctor findById(int id) {
        String sql = "SELECT id,name,specialty FROM doctors WHERE id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return new Doctor(rs.getInt("id"), rs.getString("name"), rs.getString("specialty")); }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return null;
    }

    @Override
    public List<Doctor> listAll() {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT id,name,specialty FROM doctors ORDER BY id";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new Doctor(rs.getInt("id"), rs.getString("name"), rs.getString("specialty")));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    @Override
    public Doctor updateDoctor(int id, String name, String specialty) {
        String sql = "UPDATE doctors SET name=?, specialty=? WHERE id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, specialty); ps.setInt(3, id); ps.executeUpdate();
            return new Doctor(id, name, specialty);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
