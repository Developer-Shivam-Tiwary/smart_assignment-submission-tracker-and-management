package com.smartassignment.dao;

import com.smartassignment.model.ActivityLog;
import com.smartassignment.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the ActivityLogDAO interface.
 * Handles database operations for system activity logs (audit trails).
 */
public class ActivityLogDAOImpl implements ActivityLogDAO {

    @Override
    public boolean insert(ActivityLog log) throws SQLException {
        String sql = "INSERT INTO activity_logs (actor_id, actor_role, action, entity_type, entity_id, description, ip_address, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, log.getActorId());
            ps.setString(2, log.getActorRole().name());
            ps.setString(3, log.getAction());
            ps.setString(4, log.getEntityType());
            
            if (log.getEntityId() != null) {
                ps.setInt(5, log.getEntityId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            
            ps.setString(6, log.getDescription());
            ps.setString(7, log.getIpAddress());
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        log.setLogId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public List<ActivityLog> getAll() throws SQLException {
        String sql = "SELECT log_id, actor_id, actor_role, action, entity_type, entity_id, description, ip_address, created_at " +
                     "FROM activity_logs ORDER BY created_at DESC";
        List<ActivityLog> logs = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                logs.add(mapRowToActivityLog(rs));
            }
        }
        return logs;
    }

    @Override
    public List<ActivityLog> getByActor(int actorId, ActivityLog.Role actorRole) throws SQLException {
        String sql = "SELECT log_id, actor_id, actor_role, action, entity_type, entity_id, description, ip_address, created_at " +
                     "FROM activity_logs WHERE actor_id = ? AND actor_role = ? ORDER BY created_at DESC";
        List<ActivityLog> logs = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, actorId);
            ps.setString(2, actorRole.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRowToActivityLog(rs));
                }
            }
        }
        return logs;
    }

    @Override
    public List<ActivityLog> getByEntity(String entityType, int entityId) throws SQLException {
        String sql = "SELECT log_id, actor_id, actor_role, action, entity_type, entity_id, description, ip_address, created_at " +
                     "FROM activity_logs WHERE entity_type = ? AND entity_id = ? ORDER BY created_at DESC";
        List<ActivityLog> logs = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, entityType);
            ps.setInt(2, entityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRowToActivityLog(rs));
                }
            }
        }
        return logs;
    }

    /**
     * Helper method to map a ResultSet row to an ActivityLog object.
     */
    private ActivityLog mapRowToActivityLog(ResultSet rs) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setLogId(rs.getInt("log_id"));
        log.setActorId(rs.getInt("actor_id"));
        log.setActorRole(ActivityLog.Role.valueOf(rs.getString("actor_role")));
        log.setAction(rs.getString("action"));
        log.setEntityType(rs.getString("entity_type"));
        
        int entityId = rs.getInt("entity_id");
        if (rs.wasNull()) {
            log.setEntityId(null);
        } else {
            log.setEntityId(entityId);
        }
        
        log.setDescription(rs.getString("description"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setCreatedAt(rs.getTimestamp("created_at"));
        return log;
    }
}
