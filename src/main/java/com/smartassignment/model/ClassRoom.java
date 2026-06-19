package com.smartassignment.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Model class representing a Classroom / Section (e.g. BCA 3-A, MCA 1-B).
 * Classroom records are managed by Administrators.
 */
public class ClassRoom implements Serializable {
    private static final long serialVersionUID = 1L;

    private int classId;
    private String className;
    private String section;
    private String academicYear;
    private String description;
    private boolean isActive;
    private int createdBy; // Reference to Admin ID
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Default Constructor
    public ClassRoom() {
        this.isActive = true;
    }

    // Constructor without ID (for creation)
    public ClassRoom(String className, String section, String academicYear, 
                     String description, boolean isActive, int createdBy) {
        this.className = className;
        this.section = section;
        this.academicYear = academicYear;
        this.description = description;
        this.isActive = isActive;
        this.createdBy = createdBy;
    }

    // Full Constructor
    public ClassRoom(int classId, String className, String section, String academicYear, 
                     String description, boolean isActive, int createdBy, 
                     Timestamp createdAt, Timestamp updatedAt) {
        this.classId = classId;
        this.className = className;
        this.section = section;
        this.academicYear = academicYear;
        this.description = description;
        this.isActive = isActive;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassRoom classRoom = (ClassRoom) o;
        return classId == classRoom.classId && 
               isActive == classRoom.isActive && 
               createdBy == classRoom.createdBy && 
               Objects.equals(className, classRoom.className) && 
               Objects.equals(section, classRoom.section) && 
               Objects.equals(academicYear, classRoom.academicYear) && 
               Objects.equals(description, classRoom.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classId, className, section, academicYear, description, isActive, createdBy);
    }

    @Override
    public String toString() {
        return "ClassRoom{" +
                "classId=" + classId +
                ", className='" + className + '\'' +
                ", section='" + section + '\'' +
                ", academicYear='" + academicYear + '\'' +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
