package com.smartassignment.dao;

import com.smartassignment.model.Student;
import com.smartassignment.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the StudentDAO interface.
 * Handles database operations for the Student entity.
 */
public class StudentDAOImpl implements StudentDAO {

    @Override
    public boolean insert(Student student) throws SQLException {
        String sql = "INSERT INTO students (full_name, email, password_hash, roll_number, phone, class_id, is_active, created_by, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, student.getFullName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getPasswordHash());
            ps.setString(4, student.getRollNumber());
            ps.setString(5, student.getPhone());
            ps.setInt(6, student.getClassId());
            ps.setInt(7, student.isActive() ? 1 : 0);
            ps.setInt(8, student.getCreatedBy());
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        student.setStudentId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean update(Student student) throws SQLException {
        String sql = "UPDATE students SET full_name = ?, email = ?, password_hash = ?, roll_number = ?, phone = ?, " +
                     "class_id = ?, is_active = ?, created_by = ?, updated_at = NOW() WHERE student_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, student.getFullName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getPasswordHash());
            ps.setString(4, student.getRollNumber());
            ps.setString(5, student.getPhone());
            ps.setInt(6, student.getClassId());
            ps.setInt(7, student.isActive() ? 1 : 0);
            ps.setInt(8, student.getCreatedBy());
            ps.setInt(9, student.getStudentId());
            
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE student_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Student getById(int studentId) throws SQLException {
        String sql = "SELECT student_id, full_name, email, password_hash, roll_number, phone, class_id, is_active, created_by, created_at, updated_at " +
                     "FROM students WHERE student_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToStudent(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Student getByEmail(String email) throws SQLException {
        String sql = "SELECT student_id, full_name, email, password_hash, roll_number, phone, class_id, is_active, created_by, created_at, updated_at " +
                     "FROM students WHERE email = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToStudent(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Student getByRollNumberAndClassId(String rollNumber, int classId) throws SQLException {
        String sql = "SELECT student_id, full_name, email, password_hash, roll_number, phone, class_id, is_active, created_by, created_at, updated_at " +
                     "FROM students WHERE roll_number = ? AND class_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, rollNumber);
            ps.setInt(2, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToStudent(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Student> getAll() throws SQLException {
        String sql = "SELECT student_id, full_name, email, password_hash, roll_number, phone, class_id, is_active, created_by, created_at, updated_at " +
                     "FROM students";
        List<Student> students = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                students.add(mapRowToStudent(rs));
            }
        }
        return students;
    }

    @Override
    public List<Student> getByClassId(int classId) throws SQLException {
        String sql = "SELECT student_id, full_name, email, password_hash, roll_number, phone, class_id, is_active, created_by, created_at, updated_at " +
                     "FROM students WHERE class_id = ?";
        List<Student> students = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(mapRowToStudent(rs));
                }
            }
        }
        return students;
    }

    @Override
    public List<Student> searchByClass(int classId, String keyword) throws SQLException {
        String sql = "SELECT student_id, full_name, email, password_hash, roll_number, phone, class_id, is_active, created_by, created_at, updated_at " +
                     "FROM students WHERE class_id = ? AND (full_name LIKE ? OR roll_number LIKE ?)";
        List<Student> students = new ArrayList<>();
        String likeKeyword = "%" + keyword + "%";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, classId);
            ps.setString(2, likeKeyword);
            ps.setString(3, likeKeyword);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(mapRowToStudent(rs));
                }
            }
        }
        return students;
    }

    /**
     * Helper method to map a ResultSet row to a Student object.
     */
    private Student mapRowToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setStudentId(rs.getInt("student_id"));
        student.setFullName(rs.getString("full_name"));
        student.setEmail(rs.getString("email"));
        student.setPasswordHash(rs.getString("password_hash"));
        student.setRollNumber(rs.getString("roll_number"));
        student.setPhone(rs.getString("phone"));
        student.setClassId(rs.getInt("class_id"));
        student.setActive(rs.getInt("is_active") == 1);
        student.setCreatedBy(rs.getInt("created_by"));
        student.setCreatedAt(rs.getTimestamp("created_at"));
        student.setUpdatedAt(rs.getTimestamp("updated_at"));
        return student;
    }
}
