package com.smartassignment.dao;

import com.smartassignment.model.Submission;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object (DAO) interface for Student Submission operations.
 */
public interface SubmissionDAO {
    
    /**
     * Inserts a new Submission record into the database.
     * 
     * @param submission the Submission object to insert.
     * @return true if insertion was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean insert(Submission submission) throws SQLException;
    
    /**
     * Updates an existing Submission record in the database.
     * Used for re-uploading submissions.
     * 
     * @param submission the Submission object with updated details.
     * @return true if the update was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean update(Submission submission) throws SQLException;
    
    /**
     * Deletes a Submission record from the database by ID.
     * 
     * @param submissionId the unique ID of the Submission to delete.
     * @return true if deletion was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean delete(int submissionId) throws SQLException;
    
    /**
     * Retrieves a Submission record from the database by ID.
     * 
     * @param submissionId the unique ID of the Submission to retrieve.
     * @return the Submission object if found, or null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    Submission getById(int submissionId) throws SQLException;
    
    /**
     * Retrieves a Submission record by Assignment ID and Student ID.
     * Used to check if a student has already submitted for a specific assignment.
     * 
     * @param assignmentId the unique ID of the assignment.
     * @param studentId the unique ID of the student.
     * @return the Submission object if found, or null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    Submission getByAssignmentAndStudent(int assignmentId, int studentId) throws SQLException;
    
    /**
     * Retrieves all Submissions made for a specific Assignment ID.
     * Used by teachers to view submissions.
     * 
     * @param assignmentId the unique ID of the assignment.
     * @return a list of Submission objects.
     * @throws SQLException if a database access error occurs.
     */
    List<Submission> getByAssignmentId(int assignmentId) throws SQLException;

    /**
     * Retrieves all Submissions made by a specific Student ID.
     * Used by students to view their submission history.
     * 
     * @param studentId the unique ID of the student.
     * @return a list of Submission objects.
     * @throws SQLException if a database access error occurs.
     */
    List<Submission> getByStudentId(int studentId) throws SQLException;

    /**
     * Updates the marks and feedback of a submission (Grading).
     * 
     * @param submissionId the unique ID of the submission.
     * @param marks the marks awarded.
     * @param feedback the feedback text.
     * @return true if update was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean gradeSubmission(int submissionId, BigDecimal marks, String feedback) throws SQLException;

    /**
     * Counts the number of submissions for an assignment.
     * 
     * @param assignmentId the unique ID of the assignment.
     * @return the number of submissions.
     * @throws SQLException if a database access error occurs.
     */
    int getSubmissionCount(int assignmentId) throws SQLException;

    /**
     * Counts the number of submissions with a specific status for an assignment.
     * Used for teacher statistics (e.g. counts for SUBMITTED, LATE).
     * 
     * @param assignmentId the unique ID of the assignment.
     * @param status the submission status to filter by.
     * @return the number of matching submissions.
     * @throws SQLException if a database access error occurs.
     */
    int getSubmissionCountByStatus(int assignmentId, Submission.Status status) throws SQLException;
}
