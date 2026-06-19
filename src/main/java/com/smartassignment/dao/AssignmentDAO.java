package com.smartassignment.dao;

import com.smartassignment.model.Assignment;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Data Access Object (DAO) interface for Assignment operations.
 */
public interface AssignmentDAO {
    
    /**
     * Inserts a new Assignment record into the database.
     * 
     * @param assignment the Assignment object to insert.
     * @return true if insertion was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean insert(Assignment assignment) throws SQLException;
    
    /**
     * Updates an existing Assignment record in the database.
     * 
     * @param assignment the Assignment object with updated details.
     * @return true if the update was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean update(Assignment assignment) throws SQLException;
    
    /**
     * Deletes an Assignment record from the database by ID.
     * 
     * @param assignmentId the unique ID of the Assignment to delete.
     * @return true if deletion was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean delete(int assignmentId) throws SQLException;
    
    /**
     * Retrieves an Assignment record from the database by ID.
     * 
     * @param assignmentId the unique ID of the Assignment to retrieve.
     * @return the Assignment object if found, or null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    Assignment getById(int assignmentId) throws SQLException;
    
    /**
     * Retrieves all Assignment records from the database.
     * 
     * @return a list of all Assignment objects.
     * @throws SQLException if a database access error occurs.
     */
    List<Assignment> getAll() throws SQLException;

    /**
     * Retrieves all Assignment records created by a specific teacher.
     * 
     * @param teacherId the unique ID of the teacher.
     * @return a list of Assignment objects.
     * @throws SQLException if a database access error occurs.
     */
    List<Assignment> getByTeacherId(int teacherId) throws SQLException;

    /**
     * Retrieves all Assignment records assigned to a specific class.
     * 
     * @param classId the unique ID of the class.
     * @return a list of Assignment objects.
     * @throws SQLException if a database access error occurs.
     */
    List<Assignment> getByClassId(int classId) throws SQLException;

    /**
     * Retrieves all Assignment records assigned to a specific class with a specific status.
     * Used by students to filter Active/Closed assignments.
     * 
     * @param classId the unique ID of the class.
     * @param status the assignment status (ACTIVE, CLOSED, DRAFT).
     * @return a list of Assignment objects.
     * @throws SQLException if a database access error occurs.
     */
    List<Assignment> getByClassIdAndStatus(int classId, Assignment.Status status) throws SQLException;

    /**
     * Extends the deadline of a specific assignment for all students.
     * 
     * @param assignmentId the unique ID of the assignment.
     * @param newDeadline the new extended deadline timestamp.
     * @return true if update was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean extendDeadline(int assignmentId, Timestamp newDeadline) throws SQLException;

    /**
     * Updates the status of an assignment.
     * 
     * @param assignmentId the unique ID of the assignment.
     * @param status the new Status value.
     * @return true if update was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean updateStatus(int assignmentId, Assignment.Status status) throws SQLException;
}
