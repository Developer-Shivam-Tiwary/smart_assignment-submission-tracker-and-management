package com.smartassignment.dao;

import com.smartassignment.model.Submission;
import com.smartassignment.util.DBConnection;
import java.math.BigDecimal;
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
 * Implementation of the SubmissionDAO interface.
 * Handles database operations for the Submission entity.
 */
public class SubmissionDAOImpl implements SubmissionDAO {

    @Override
    public boolean insert(Submission submission) throws SQLException {
        String sql = "INSERT INTO submissions (assignment_id, student_id, file_name, file_path, file_size_bytes, " +
                     "file_type, submitted_at, resubmitted_at, resubmit_count, status, marks, feedback, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?, NOW(), NOW())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, submission.getAssignmentId());
            ps.setInt(2, submission.getStudentId());
            ps.setString(3, submission.getFileName());
            ps.setString(4, submission.getFilePath());
            ps.setLong(5, submission.getFileSizeBytes());
            ps.setString(6, submission.getFileType());
            
            if (submission.getResubmittedAt() != null) {
                ps.setTimestamp(7, submission.getResubmittedAt());
            } else {
                ps.setNull(7, Types.TIMESTAMP);
            }
            
            ps.setInt(8, submission.getResubmitCount());
            ps.setString(9, submission.getStatus().name());
            
            if (submission.getMarks() != null) {
                ps.setBigDecimal(10, submission.getMarks());
            } else {
                ps.setNull(10, Types.DECIMAL);
            }
            
            ps.setString(11, submission.getFeedback());
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        submission.setSubmissionId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean update(Submission submission) throws SQLException {
        String sql = "UPDATE submissions SET file_name = ?, file_path = ?, file_size_bytes = ?, file_type = ?, " +
                     "resubmitted_at = NOW(), resubmit_count = ?, status = ?, marks = ?, feedback = ?, updated_at = NOW() " +
                     "WHERE submission_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, submission.getFileName());
            ps.setString(2, submission.getFilePath());
            ps.setLong(3, submission.getFileSizeBytes());
            ps.setString(4, submission.getFileType());
            ps.setInt(5, submission.getResubmitCount());
            ps.setString(6, submission.getStatus().name());
            
            if (submission.getMarks() != null) {
                ps.setBigDecimal(7, submission.getMarks());
            } else {
                ps.setNull(7, Types.DECIMAL);
            }
            
            ps.setString(8, submission.getFeedback());
            ps.setInt(9, submission.getSubmissionId());
            
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int submissionId) throws SQLException {
        String sql = "DELETE FROM submissions WHERE submission_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, submissionId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Submission getById(int submissionId) throws SQLException {
        String sql = "SELECT submission_id, assignment_id, student_id, file_name, file_path, file_size_bytes, file_type, " +
                     "submitted_at, resubmitted_at, resubmit_count, status, marks, feedback, marks_updated_at, feedback_updated_at, " +
                     "created_at, updated_at FROM submissions WHERE submission_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, submissionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSubmission(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Submission getByAssignmentAndStudent(int assignmentId, int studentId) throws SQLException {
        String sql = "SELECT submission_id, assignment_id, student_id, file_name, file_path, file_size_bytes, file_type, " +
                     "submitted_at, resubmitted_at, resubmit_count, status, marks, feedback, marks_updated_at, feedback_updated_at, " +
                     "created_at, updated_at FROM submissions WHERE assignment_id = ? AND student_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, assignmentId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSubmission(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Submission> getByAssignmentId(int assignmentId) throws SQLException {
        String sql = "SELECT submission_id, assignment_id, student_id, file_name, file_path, file_size_bytes, file_type, " +
                     "submitted_at, resubmitted_at, resubmit_count, status, marks, feedback, marks_updated_at, feedback_updated_at, " +
                     "created_at, updated_at FROM submissions WHERE assignment_id = ? ORDER BY submitted_at DESC";
        List<Submission> submissions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, assignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    submissions.add(mapRowToSubmission(rs));
                }
            }
        }
        return submissions;
    }

    @Override
    public List<Submission> getByStudentId(int studentId) throws SQLException {
        String sql = "SELECT submission_id, assignment_id, student_id, file_name, file_path, file_size_bytes, file_type, " +
                     "submitted_at, resubmitted_at, resubmit_count, status, marks, feedback, marks_updated_at, feedback_updated_at, " +
                     "created_at, updated_at FROM submissions WHERE student_id = ? ORDER BY submitted_at DESC";
        List<Submission> submissions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    submissions.add(mapRowToSubmission(rs));
                }
            }
        }
        return submissions;
    }

    @Override
    public boolean gradeSubmission(int submissionId, BigDecimal marks, String feedback) throws SQLException {
        String sql = "UPDATE submissions SET marks = ?, feedback = ?, marks_updated_at = NOW(), feedback_updated_at = NOW(), updated_at = NOW() " +
                     "WHERE submission_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (marks != null) {
                ps.setBigDecimal(1, marks);
            } else {
                ps.setNull(1, Types.DECIMAL);
            }
            
            ps.setString(2, feedback);
            ps.setInt(3, submissionId);
            
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int getSubmissionCount(int assignmentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM submissions WHERE assignment_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, assignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    @Override
    public int getSubmissionCountByStatus(int assignmentId, Submission.Status status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM submissions WHERE assignment_id = ? AND status = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, assignmentId);
            ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Helper method to map a ResultSet row to a Submission object.
     */
    private Submission mapRowToSubmission(ResultSet rs) throws SQLException {
        Submission submission = new Submission();
        submission.setSubmissionId(rs.getInt("submission_id"));
        submission.setAssignmentId(rs.getInt("assignment_id"));
        submission.setStudentId(rs.getInt("student_id"));
        submission.setFileName(rs.getString("file_name"));
        submission.setFilePath(rs.getString("file_path"));
        submission.setFileSizeBytes(rs.getLong("file_size_bytes"));
        submission.setFileType(rs.getString("file_type"));
        submission.setSubmittedAt(rs.getTimestamp("submitted_at"));
        submission.setResubmittedAt(rs.getTimestamp("resubmitted_at"));
        submission.setResubmitCount(rs.getInt("resubmit_count"));
        submission.setStatus(Submission.Status.valueOf(rs.getString("status")));
        submission.setMarks(rs.getBigDecimal("marks"));
        submission.setFeedback(rs.getString("feedback"));
        submission.setMarksUpdatedAt(rs.getTimestamp("marks_updated_at"));
        submission.setFeedbackUpdatedAt(rs.getTimestamp("feedback_updated_at"));
        submission.setCreatedAt(rs.getTimestamp("created_at"));
        submission.setUpdatedAt(rs.getTimestamp("updated_at"));
        return submission;
    }
}
