package com.smartassignment.dao;

import com.smartassignment.model.ClassRoom;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object (DAO) interface for Classroom operations.
 */
public interface ClassRoomDAO {
    
    /**
     * Inserts a new ClassRoom record into the database.
     * 
     * @param classRoom the ClassRoom object to insert.
     * @return true if insertion was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean insert(ClassRoom classRoom) throws SQLException;
    
    /**
     * Updates an existing ClassRoom record in the database.
     * 
     * @param classRoom the ClassRoom object with updated details.
     * @return true if the update was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean update(ClassRoom classRoom) throws SQLException;
    
    /**
     * Deletes a ClassRoom record from the database by ID.
     * 
     * @param classId the unique ID of the ClassRoom to delete.
     * @return true if deletion was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean delete(int classId) throws SQLException;
    
    /**
     * Retrieves a ClassRoom record from the database by ID.
     * 
     * @param classId the unique ID of the ClassRoom to retrieve.
     * @return the ClassRoom object if found, or null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    ClassRoom getById(int classId) throws SQLException;
    
    /**
     * Retrieves a ClassRoom record by Name, Section, and Academic Year.
     * Used to enforce unique constraints.
     * 
     * @param className the name of the classroom (e.g. BCA).
     * @param section the section (e.g. 3-A).
     * @param academicYear the academic year (e.g. 2025-2026).
     * @return the ClassRoom object if found, or null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    ClassRoom getByNameSectionAndAcademicYear(String className, String section, String academicYear) throws SQLException;
    
    /**
     * Retrieves all ClassRoom records from the database.
     * 
     * @return a list of all ClassRoom objects.
     * @throws SQLException if a database access error occurs.
     */
    List<ClassRoom> getAll() throws SQLException;

    /**
     * Retrieves all ClassRoom records assigned to a specific teacher.
     * 
     * @param teacherId the unique ID of the teacher.
     * @return a list of ClassRoom objects assigned to the teacher.
     * @throws SQLException if a database access error occurs.
     */
    List<ClassRoom> getClassesByTeacherId(int teacherId) throws SQLException;
}
