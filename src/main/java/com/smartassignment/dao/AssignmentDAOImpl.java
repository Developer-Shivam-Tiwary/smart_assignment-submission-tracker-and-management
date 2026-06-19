package com.smartassignment.dao;

import com.smartassignment.model.Assignment;
import com.smartassignment.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the AssignmentDAO interface.
 * Handles database operations for the Assignment entity.
 */
public class AssignmentDAOImpl implements AssignmentDAO {

    @Override
    public boolean insert(Assignment assignment) throws SQLException {
        String sql = "INSERT INTO assignments (title, description, class_id, teacher_id, file_name, file_path, " +
                     "file_size_bytes, file_type, deadline, extended_deadline, max_marks, status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, assignment.getTitle());
            ps.setString(2, assignment.getDescription());
            ps.setInt(3, assignment.getClassId());
            ps.setInt(4, assignment.getTeacherId());
            
            ps.setString(5, assignment.getFileName());
            ps.setString(6, assignment.getFilePath());
            
            if (assignment.getFileSizeBytes() != null) {
                ps.setLong(7, assignment.getFileSizeBytes());
            } else {
                ps.setNull(7, Types.BIGINT);
            }
            
            ps.setString(8, assignment.getFileType());
            ps.setTimestamp(9, assignment.getDeadline());
            ps.setTimestamp(10, assignment.getExtendedDeadline());
            ps.setInt(11, assignment.getMaxMarks());
            ps.setString(12, assignment.getStatus().name());
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        assignment.setAssignmentId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean update(Assignment assignment) throws SQLException {
        String sql = "UPDATE assignments SET title = ?, description = ?, class_id = ?, teacher_id = ?, " +
                     "file_name = ?, file_path = ?, file_size_bytes = ?, file_type = ?, deadline = ?, " +
                     "extended_deadline = ?, max_marks = ?, status = ?, updated_at = NOW() WHERE assignment_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, assignment.getTitle());
            ps.setString(2, assignment.getDescription());
            ps.setInt(3, assignment.getClassId());
            ps.setInt(4, assignment.getTeacherId());
            
            ps.setString(5, assignment.getFileName());
            ps.setString(6, assignment.getFilePath());
            
            if (assignment.getFileSizeBytes() != null) {
                ps.setLong(7, assignment.getFileSizeBytes());
            } else {
                ps.setNull(7, Types.BIGINT);
            }
            
            ps.setString(8, assignment.getFileType());
            ps.setTimestamp(9, assignment.getDeadline());
            ps.setTimestamp(10, assignment.getExtendedDeadline());
            ps.setInt(11, assignment.getMaxMarks());
            ps.setString(12, assignment.getStatus().name());
            ps.setInt(13, assignment.getAssignmentId());
            
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int assignmentId) throws SQLException {
        String sql = "DELETE FROM assignments WHERE assignment_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, assignmentId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Assignment getById(int assignmentId) throws SQLException {
        String sql = "SELECT assignment_id, title, description, class_id, teacher_id, file_name, file_path, " +
                     "file_size_bytes, file_type, deadline, extended_deadline, max_marks, status, created_at, updated_at " +
                     "FROM assignments WHERE assignment_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, assignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToAssignment(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Assignment> getAll() throws SQLException {
        String sql = "SELECT assignment_id, title, description, class_id, teacher_id, file_name, file_path, " +
                     "file_size_bytes, file_type, deadline, extended_deadline, max_marks, status, created_at, updated_at " +
                     "FROM assignments";
        List<Assignment> assignments = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                assignments.add(mapRowToAssignment(rs));
            }
        }
        return assignments;
    }

    @Override
    public List<Assignment> getByTeacherId(int teacherId) throws SQLException {
        String sql = "SELECT assignment_id, title, description, class_id, teacher_id, file_name, file_path, " +
                     "file_size_bytes, file_type, deadline, extended_deadline, max_marks, status, created_at, updated_at " +
                     "FROM assignments WHERE teacher_id = ? ORDER BY created_at DESC";
        List<Assignment> assignments = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    assignments.add(mapRowToAssignment(rs));
                }
            }
        }
        return assignments;
    }

    @Override
    public List<Assignment> getByClassId(int classId) throws SQLException {
        String sql = "SELECT assignment_id, title, description, class_id, teacher_id, file_name, file_path, " +
                     "file_size_bytes, file_type, deadline, extended_deadline, max_marks, status, created_at, updated_at " +
                     "FROM assignments WHERE class_id = ? ORDER BY created_at DESC";
        List<Assignment> assignments = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    assignments.add(mapRowToAssignment(rs));
                }
            }
        }
        return assignments;
    }

    @Override
    public List<Assignment> getByClassIdAndStatus(int classId, Assignment.Status status) throws SQLException {
        String sql = "SELECT assignment_id, title, description, class_id, teacher_id, file_name, file_path, " +
                     "file_size_bytes, file_type, deadline, extended_deadline, max_marks, status, created_at, updated_at " +
                     "FROM assignments WHERE class_id = ? AND status = ? ORDER BY created_at DESC";
        List<Assignment> assignments = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, classId);
            ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    assignments.add(mapRowToAssignment(rs));
                }
            }
        }
        return assignments;
    }

    @Override
    public boolean extendDeadline(int assignmentId, Timestamp newDeadline) throws SQLException {
        String sql = "UPDATE assignments SET extended_deadline = ?, updated_at = NOW() WHERE assignment_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setTimestamp(1, newDeadline);
            ps.setInt(2, assignmentId);
            
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(int assignmentId, Assignment.Status status) throws SQLException {
        String sql = "UPDATE assignments SET status = ?, updated_at = NOW() WHERE assignment_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status.name());
            ps.setInt(2, assignmentId);
            
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Helper method to map a ResultSet row to an Assignment object.
     */
    private Assignment mapRowToAssignment(ResultSet rs) throws SQLException {
        Assignment assignment = new Assignment();
        assignment.setAssignmentId(rs.getInt("assignment_id"));
        assignment.setTitle(rs.getString("title"));
        assignment.setDescription(rs.getString("description"));
        assignment.setClassId(rs.getInt("class_id"));
        assignment.setTeacherId(rs.getInt("teacher_id"));
        assignment.setFileName(rs.getString("file_name"));
        assignment.setFilePath(rs.getString("file_path"));
        
        long size = rs.getLong("file_size_bytes");
        if (rs.wasNull()) {
            assignment.setFileSizeBytes(null);
        } else {
            assignment.setFileSizeBytes(size);
        }
        
        assignment.setFileType(rs.getString("file_type"));
        assignment.setDeadline(rs.getTimestamp("deadline"));
        assignment.setExtendedDeadline(rs.getTimestamp("extended_deadline"));
        assignment.setMaxMarks(rs.getInt("max_marks"));
        assignment.setStatus(Assignment.Status.valueOf(rs.getString("status")));
        assignment.setCreatedAt(rs.getTimestamp("created_at"));
        assignment.setUpdatedAt(rs.getTimestamp("updated_at"));
        
        return assignment;
    }
}
