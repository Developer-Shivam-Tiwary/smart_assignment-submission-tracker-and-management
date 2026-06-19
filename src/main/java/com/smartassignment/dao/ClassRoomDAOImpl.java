package com.smartassignment.dao;

import com.smartassignment.model.ClassRoom;
import com.smartassignment.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the ClassRoomDAO interface.
 * Handles database operations for the ClassRoom entity (classes table).
 */
public class ClassRoomDAOImpl implements ClassRoomDAO {

    @Override
    public boolean insert(ClassRoom classRoom) throws SQLException {
        String sql = "INSERT INTO classes (class_name, section, academic_year, description, is_active, created_by, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, classRoom.getClassName());
            ps.setString(2, classRoom.getSection());
            ps.setString(3, classRoom.getAcademicYear());
            ps.setString(4, classRoom.getDescription());
            ps.setInt(5, classRoom.isActive() ? 1 : 0);
            ps.setInt(6, classRoom.getCreatedBy());
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        classRoom.setClassId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean update(ClassRoom classRoom) throws SQLException {
        String sql = "UPDATE classes SET class_name = ?, section = ?, academic_year = ?, description = ?, " +
                     "is_active = ?, created_by = ?, updated_at = NOW() WHERE class_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, classRoom.getClassName());
            ps.setString(2, classRoom.getSection());
            ps.setString(3, classRoom.getAcademicYear());
            ps.setString(4, classRoom.getDescription());
            ps.setInt(5, classRoom.isActive() ? 1 : 0);
            ps.setInt(6, classRoom.getCreatedBy());
            ps.setInt(7, classRoom.getClassId());
            
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int classId) throws SQLException {
        String sql = "DELETE FROM classes WHERE class_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, classId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public ClassRoom getById(int classId) throws SQLException {
        String sql = "SELECT class_id, class_name, section, academic_year, description, is_active, created_by, created_at, updated_at " +
                     "FROM classes WHERE class_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToClassRoom(rs);
                }
            }
        }
        return null;
    }

    @Override
    public ClassRoom getByNameSectionAndAcademicYear(String className, String section, String academicYear) throws SQLException {
        String sql = "SELECT class_id, class_name, section, academic_year, description, is_active, created_by, created_at, updated_at " +
                     "FROM classes WHERE class_name = ? AND section = ? AND academic_year = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, className);
            ps.setString(2, section);
            ps.setString(3, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToClassRoom(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<ClassRoom> getAll() throws SQLException {
        String sql = "SELECT class_id, class_name, section, academic_year, description, is_active, created_by, created_at, updated_at " +
                     "FROM classes";
        List<ClassRoom> classRooms = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                classRooms.add(mapRowToClassRoom(rs));
            }
        }
        return classRooms;
    }

    @Override
    public List<ClassRoom> getClassesByTeacherId(int teacherId) throws SQLException {
        String sql = "SELECT c.class_id, c.class_name, c.section, c.academic_year, c.description, c.is_active, c.created_by, c.created_at, c.updated_at " +
                     "FROM classes c JOIN teacher_classes tc ON c.class_id = tc.class_id WHERE tc.teacher_id = ?";
        List<ClassRoom> classRooms = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    classRooms.add(mapRowToClassRoom(rs));
                }
            }
        }
        return classRooms;
    }

    /**
     * Helper method to map a ResultSet row to a ClassRoom object.
     */
    private ClassRoom mapRowToClassRoom(ResultSet rs) throws SQLException {
        ClassRoom classRoom = new ClassRoom();
        classRoom.setClassId(rs.getInt("class_id"));
        classRoom.setClassName(rs.getString("class_name"));
        classRoom.setSection(rs.getString("section"));
        classRoom.setAcademicYear(rs.getString("academic_year"));
        classRoom.setDescription(rs.getString("description"));
        classRoom.setActive(rs.getInt("is_active") == 1);
        classRoom.setCreatedBy(rs.getInt("created_by"));
        classRoom.setCreatedAt(rs.getTimestamp("created_at"));
        classRoom.setUpdatedAt(rs.getTimestamp("updated_at"));
        return classRoom;
    }
}
