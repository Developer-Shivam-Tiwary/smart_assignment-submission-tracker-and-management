package com.smartassignment.dao;

import com.smartassignment.model.Admin;
import com.smartassignment.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the AdminDAO interface.
 * Handles database operations for the Admin entity.
 */
public class AdminDAOImpl implements AdminDAO {

    @Override
    public boolean insert(Admin admin) throws SQLException {
        String sql = "INSERT INTO admins (full_name, email, password_hash, phone, is_active, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, admin.getFullName());
            ps.setString(2, admin.getEmail());
            ps.setString(3, admin.getPasswordHash());
            ps.setString(4, admin.getPhone());
            ps.setInt(5, admin.isActive() ? 1 : 0);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        admin.setAdminId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean update(Admin admin) throws SQLException {
        String sql = "UPDATE admins SET full_name = ?, email = ?, password_hash = ?, phone = ?, is_active = ?, updated_at = NOW() " +
                     "WHERE admin_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, admin.getFullName());
            ps.setString(2, admin.getEmail());
            ps.setString(3, admin.getPasswordHash());
            ps.setString(4, admin.getPhone());
            ps.setInt(5, admin.isActive() ? 1 : 0);
            ps.setInt(6, admin.getAdminId());
            
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int adminId) throws SQLException {
        String sql = "DELETE FROM admins WHERE admin_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, adminId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Admin getById(int adminId) throws SQLException {
        String sql = "SELECT admin_id, full_name, email, password_hash, phone, is_active, created_at, updated_at " +
                     "FROM admins WHERE admin_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, adminId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToAdmin(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Admin getByEmail(String email) throws SQLException {
        String sql = "SELECT admin_id, full_name, email, password_hash, phone, is_active, created_at, updated_at " +
                     "FROM admins WHERE email = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToAdmin(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Admin> getAll() throws SQLException {
        String sql = "SELECT admin_id, full_name, email, password_hash, phone, is_active, created_at, updated_at " +
                     "FROM admins";
        List<Admin> admins = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                admins.add(mapRowToAdmin(rs));
            }
        }
        return admins;
    }

    /**
     * Helper method to map a ResultSet row to an Admin object.
     */
    private Admin mapRowToAdmin(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setAdminId(rs.getInt("admin_id"));
        admin.setFullName(rs.getString("full_name"));
        admin.setEmail(rs.getString("email"));
        admin.setPasswordHash(rs.getString("password_hash"));
        admin.setPhone(rs.getString("phone"));
        admin.setActive(rs.getInt("is_active") == 1);
        admin.setCreatedAt(rs.getTimestamp("created_at"));
        admin.setUpdatedAt(rs.getTimestamp("updated_at"));
        return admin;
    }
}
