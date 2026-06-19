package com.smartassignment.dao;

import com.smartassignment.model.ActivityLog;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object (DAO) interface for ActivityLog (Audit Log) operations.
 */
public interface ActivityLogDAO {
    
    /**
     * Inserts a new Activity Log record into the database.
     * 
     * @param log the ActivityLog object to insert.
     * @return true if insertion was successful, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean insert(ActivityLog log) throws SQLException;
    
    /**
     * Retrieves all Activity Log records from the database, ordered by creation time descending.
     * 
     * @return a list of all ActivityLog objects.
     * @throws SQLException if a database access error occurs.
     */
    List<ActivityLog> getAll() throws SQLException;

    /**
     * Retrieves all Activity Log records for a specific actor.
     * 
     * @param actorId the unique ID of the user performing actions.
     * @param actorRole the role of the user (ADMIN, TEACHER, STUDENT).
     * @return a list of ActivityLog objects.
     * @throws SQLException if a database access error occurs.
     */
    List<ActivityLog> getByActor(int actorId, ActivityLog.Role actorRole) throws SQLException;

    /**
     * Retrieves Activity Log records for a specific entity type and ID.
     * Used for auditing changes to specific assignments or submissions.
     * 
     * @param entityType the type of the entity (e.g. ASSIGNMENT, SUBMISSION).
     * @param entityId the unique ID of the entity.
     * @return a list of ActivityLog objects.
     * @throws SQLException if a database access error occurs.
     */
    List<ActivityLog> getByEntity(String entityType, int entityId) throws SQLException;
}
