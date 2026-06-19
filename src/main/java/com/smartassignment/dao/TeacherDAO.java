package com.smartassignment.dao;

import com.smartassignment.model.Teacher;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object (DAO) interface for Teacher operations.
 */
public interface TeacherDAO {
    
    /**
     * Inserts a new Teacher record into the database.
     * 
     * @param teacher the Teacher object to insert.
     * @return true if insertion was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean insert(Teacher teacher) throws SQLException;
    
    /**
     * Updates an existing Teacher record in the database.
     * 
     * @param teacher the Teacher object with updated details.
     * @return true if the update was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean update(Teacher teacher) throws SQLException;
    
    /**
     * Deletes a Teacher record from the database by ID.
     * 
     * @param teacherId the unique ID of the Teacher to delete.
     * @return true if deletion was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean delete(int teacherId) throws SQLException;
    
    /**
     * Retrieves a Teacher record from the database by ID.
     * 
     * @param teacherId the unique ID of the Teacher to retrieve.
     * @return the Teacher object if found, or null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    Teacher getById(int teacherId) throws SQLException;
    
    /**
     * Retrieves a Teacher record from the database by Email address.
     * Used for authentication and duplicate checks.
     * 
     * @param email the email address of the Teacher.
     * @return the Teacher object if found, or null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    Teacher getByEmail(String email) throws SQLException;

    /**
     * Retrieves a Teacher record from the database by Employee Code.
     * Used for duplicate checks.
     * 
     * @param employeeCode the employee code of the Teacher.
     * @return the Teacher object if found, or null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    Teacher getByEmployeeCode(String employeeCode) throws SQLException;
    
    /**
     * Retrieves all Teacher records from the database.
     * 
     * @return a list of all Teacher objects.
     * @throws SQLException if a database access error occurs.
     */
    List<Teacher> getAll() throws SQLException;

    /**
     * Assigns a teacher to a specific class in the junction table.
     * 
     * @param teacherId the unique ID of the teacher.
     * @param classId the unique ID of the class.
     * @return true if the assignment was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean assignToClass(int teacherId, int classId) throws SQLException;

    /**
     * Removes a teacher assignment from a specific class in the junction table.
     * 
     * @param teacherId the unique ID of the teacher.
     * @param classId the unique ID of the class.
     * @return true if the removal was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean removeFromClass(int teacherId, int classId) throws SQLException;

    /**
     * Gets all class IDs assigned to a specific teacher.
     * 
     * @param teacherId the unique ID of the teacher.
     * @return a list of class IDs assigned to the teacher.
     * @throws SQLException if a database access error occurs.
     */
    List<Integer> getAssignedClassIds(int teacherId) throws SQLException;
}
