package com.smartassignment.dao;

import com.smartassignment.model.Teacher;
import com.smartassignment.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the TeacherDAO interface.
 * Handles database operations for the Teacher entity.
 */
public class TeacherDAOImpl implements TeacherDAO {

    @Override
    public boolean insert(Teacher teacher) throws SQLException {
        String sql = "INSERT INTO teachers (full_name, email, password_hash, phone, subject, employee_code, is_active, created_by, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, teacher.getFullName());
            ps.setString(2, teacher.getEmail());
            ps.setString(3, teacher.getPasswordHash());
            ps.setString(4, teacher.getPhone());
            ps.setString(5, teacher.getSubject());
            ps.setString(6, teacher.getEmployeeCode());
            ps.setInt(7, teacher.isActive() ? 1 : 0);
            ps.setInt(8, teacher.getCreatedBy());
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        teacher.setTeacherId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean update(Teacher teacher) throws SQLException {
        String sql = "UPDATE teachers SET full_name = ?, email = ?, password_hash = ?, phone = ?, subject = ?, " +
                     "employee_code = ?, is_active = ?, created_by = ?, updated_at = NOW() WHERE teacher_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, teacher.getFullName());
            ps.setString(2, teacher.getEmail());
            ps.setString(3, teacher.getPasswordHash());
            ps.setString(4, teacher.getPhone());
            ps.setString(5, teacher.getSubject());
            ps.setString(6, teacher.getEmployeeCode());
            ps.setInt(7, teacher.isActive() ? 1 : 0);
            ps.setInt(8, teacher.getCreatedBy());
            ps.setInt(9, teacher.getTeacherId());
            
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int teacherId) throws SQLException {
        String sql = "DELETE FROM teachers WHERE teacher_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, teacherId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Teacher getById(int teacherId) throws SQLException {
        String sql = "SELECT teacher_id, full_name, email, password_hash, phone, subject, employee_code, is_active, created_by, created_at, updated_at " +
                     "FROM teachers WHERE teacher_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToTeacher(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Teacher getByEmail(String email) throws SQLException {
        String sql = "SELECT teacher_id, full_name, email, password_hash, phone, subject, employee_code, is_active, created_by, created_at, updated_at " +
                     "FROM teachers WHERE email = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToTeacher(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Teacher getByEmployeeCode(String employeeCode) throws SQLException {
        String sql = "SELECT teacher_id, full_name, email, password_hash, phone, subject, employee_code, is_active, created_by, created_at, updated_at " +
                     "FROM teachers WHERE employee_code = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, employeeCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToTeacher(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Teacher> getAll() throws SQLException {
        String sql = "SELECT teacher_id, full_name, email, password_hash, phone, subject, employee_code, is_active, created_by, created_at, updated_at " +
                     "FROM teachers";
        List<Teacher> teachers = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                teachers.add(mapRowToTeacher(rs));
            }
        }
        return teachers;
    }

    @Override
    public boolean assignToClass(int teacherId, int classId) throws SQLException {
        String sql = "INSERT IGNORE INTO teacher_classes (teacher_id, class_id, assigned_at) VALUES (?, ?, NOW())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, teacherId);
            ps.setInt(2, classId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean removeFromClass(int teacherId, int classId) throws SQLException {
        String sql = "DELETE FROM teacher_classes WHERE teacher_id = ? AND class_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, teacherId);
            ps.setInt(2, classId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Integer> getAssignedClassIds(int teacherId) throws SQLException {
        String sql = "SELECT class_id FROM teacher_classes WHERE teacher_id = ?";
        List<Integer> classIds = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    classIds.add(rs.getInt("class_id"));
                }
            }
        }
        return classIds;
    }

    /**
     * Helper method to map a ResultSet row to a Teacher object.
     */
    private Teacher mapRowToTeacher(ResultSet rs) throws SQLException {
        Teacher teacher = new Teacher();
        teacher.setTeacherId(rs.getInt("teacher_id"));
        teacher.setFullName(rs.getString("full_name"));
        teacher.setEmail(rs.getString("email"));
        teacher.setPasswordHash(rs.getString("password_hash"));
        teacher.setPhone(rs.getString("phone"));
        teacher.setSubject(rs.getString("subject"));
        teacher.setEmployeeCode(rs.getString("employee_code"));
        teacher.setActive(rs.getInt("is_active") == 1);
        teacher.setCreatedBy(rs.getInt("created_by"));
        teacher.setCreatedAt(rs.getTimestamp("created_at"));
        teacher.setUpdatedAt(rs.getTimestamp("updated_at"));
        return teacher;
    }
}
