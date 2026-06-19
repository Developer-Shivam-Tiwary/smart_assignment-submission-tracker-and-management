package com.smartassignment.dao;

import com.smartassignment.model.Admin;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object (DAO) interface for Admin operations.
 */
public interface AdminDAO {
    
    /**
     * Inserts a new Admin record into the database.
     * 
     * @param admin the Admin object to insert.
     * @return true if insertion was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean insert(Admin admin) throws SQLException;
    
    /**
     * Updates an existing Admin record in the database.
     * 
     * @param admin the Admin object with updated details.
     * @return true if the update was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean update(Admin admin) throws SQLException;
    
    /**
     * Deletes an Admin record from the database by ID.
     * 
     * @param adminId the unique ID of the Admin to delete.
     * @return true if deletion was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean delete(int adminId) throws SQLException;
    
    /**
     * Retrieves an Admin record from the database by ID.
     * 
     * @param adminId the unique ID of the Admin to retrieve.
     * @return the Admin object if found, or null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    Admin getById(int adminId) throws SQLException;
    
    /**
     * Retrieves an Admin record from the database by Email address.
     * Used for authentication and duplicate checks.
     * 
     * @param email the email address of the Admin.
     * @return the Admin object if found, or null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    Admin getByEmail(String email) throws SQLException;
    
    /**
     * Retrieves all Admin records from the database.
     * 
     * @return a list of all Admin objects.
     * @throws SQLException if a database access error occurs.
     */
    List<Admin> getAll() throws SQLException;
}
