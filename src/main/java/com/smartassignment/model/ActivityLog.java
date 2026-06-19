package com.smartassignment.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Model class representing an Activity Log entry (audit trail).
 * Logs contain information about which user performed what action, when, and from what IP address.
 */
public class ActivityLog implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Role {
        ADMIN,
        TEACHER,
        STUDENT
    }

    private int logId;
    private int actorId;
    private Role actorRole;
    private String action;
    private String entityType;
    private Integer entityId; // Nullable
    private String description;
    private String ipAddress;
    private Timestamp createdAt;

    // Default Constructor
    public ActivityLog() {
    }

    // Constructor without ID (for log creation)
    public ActivityLog(int actorId, Role actorRole, String action, String entityType, 
                       Integer entityId, String description, String ipAddress) {
        this.actorId = actorId;
        this.actorRole = actorRole;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.description = description;
        this.ipAddress = ipAddress;
    }

    // Full Constructor
    public ActivityLog(int logId, int actorId, Role actorRole, String action, String entityType, 
                       Integer entityId, String description, String ipAddress, Timestamp createdAt) {
        this.logId = logId;
        this.actorId = actorId;
        this.actorRole = actorRole;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.description = description;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getActorId() {
        return actorId;
    }

    public void setActorId(int actorId) {
        this.actorId = actorId;
    }

    public Role getActorRole() {
        return actorRole;
    }

    public void setActorRole(Role actorRole) {
        this.actorRole = actorRole;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityLog that = (ActivityLog) o;
        return logId == that.logId && 
               actorId == that.actorId && 
               actorRole == that.actorRole && 
               Objects.equals(action, that.action) && 
               Objects.equals(entityType, that.entityType) && 
               Objects.equals(entityId, that.entityId) && 
               Objects.equals(description, that.description) && 
               Objects.equals(ipAddress, that.ipAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logId, actorId, actorRole, action, entityType, entityId, description, ipAddress);
    }

    @Override
    public String toString() {
        return "ActivityLog{" +
                "logId=" + logId +
                ", actorId=" + actorId +
                ", actorRole=" + actorRole +
                ", action='" + action + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", ipAddress='" + ipAddress + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
