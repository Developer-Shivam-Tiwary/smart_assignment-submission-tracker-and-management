package com.smartassignment.dao;

import com.smartassignment.model.Student;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object (DAO) interface for Student operations.
 */
public interface StudentDAO {
    
    /**
     * Inserts a new Student record into the database.
     * 
     * @param student the Student object to insert.
     * @return true if insertion was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean insert(Student student) throws SQLException;
    
    /**
     * Updates an existing Student record in the database.
     * 
     * @param student the Student object with updated details.
     * @return true if the update was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean update(Student student) throws SQLException;
    
    /**
     * Deletes a Student record from the database by ID.
     * 
     * @param studentId the unique ID of the Student to delete.
     * @return true if deletion was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean delete(int studentId) throws SQLException;
    
    /**
     * Retrieves a Student record from the database by ID.
     * 
     * @param studentId the unique ID of the Student to retrieve.
     * @return the Student object if found, or null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    Student getById(int studentId) throws SQLException;
    
    /**
     * Retrieves a Student record from the database by Email address.
     * Used for authentication and duplicate checks.
     * 
     * @param email the email address of the Student.
     * @return the Student object if found, or null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    Student getByEmail(String email) throws SQLException;

    /**
     * Retrieves a Student record from the database by Roll Number and Class ID.
     * Used to enforce uniqueness constraints.
     * 
     * @param rollNumber the roll number of the student.
     * @param classId the classroom ID the student belongs to.
     * @return the Student object if found, or null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    Student getByRollNumberAndClassId(String rollNumber, int classId) throws SQLException;
    
    /**
     * Retrieves all Student records from the database.
     * 
     * @return a list of all Student objects.
     * @throws SQLException if a database access error occurs.
     */
    List<Student> getAll() throws SQLException;

    /**
     * Retrieves all Student records belonging to a specific classroom ID.
     * 
     * @param classId the classroom ID.
     * @return a list of Student objects.
     * @throws SQLException if a database access error occurs.
     */
    List<Student> getByClassId(int classId) throws SQLException;

    /**
     * Searches students by name or roll number inside a specific classroom.
     * 
     * @param classId the classroom ID.
     * @param keyword the search keyword (matches name or roll number).
     * @return a list of matching Student objects.
     * @throws SQLException if a database access error occurs.
     */
    List<Student> searchByClass(int classId, String keyword) throws SQLException;
}
