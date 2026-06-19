package com.smartassignment.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Model class representing an Assignment created by a teacher.
 */
public class Assignment implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status {
        ACTIVE,
        CLOSED,
        DRAFT
    }

    private int assignmentId;
    private String title;
    private String description;
    private int classId;
    private int teacherId;
    private String fileName;
    private String filePath;
    private Long fileSizeBytes; // Can be null if no file uploaded
    private String fileType;      // Can be null
    private Timestamp deadline;
    private Timestamp extendedDeadline; // Can be null
    private int maxMarks;
    private Status status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Default Constructor
    public Assignment() {
        this.maxMarks = 100;
        this.status = Status.ACTIVE;
    }

    // Constructor without ID (for creation)
    public Assignment(String title, String description, int classId, int teacherId, 
                      String fileName, String filePath, Long fileSizeBytes, String fileType, 
                      Timestamp deadline, Timestamp extendedDeadline, int maxMarks, Status status) {
        this.title = title;
        this.description = description;
        this.classId = classId;
        this.teacherId = teacherId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSizeBytes = fileSizeBytes;
        this.fileType = fileType;
        this.deadline = deadline;
        this.extendedDeadline = extendedDeadline;
        this.maxMarks = maxMarks;
        this.status = status;
    }

    // Full Constructor
    public Assignment(int assignmentId, String title, String description, int classId, int teacherId, 
                      String fileName, String filePath, Long fileSizeBytes, String fileType, 
                      Timestamp deadline, Timestamp extendedDeadline, int maxMarks, Status status, 
                      Timestamp createdAt, Timestamp updatedAt) {
        this.assignmentId = assignmentId;
        this.title = title;
        this.description = description;
        this.classId = classId;
        this.teacherId = teacherId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSizeBytes = fileSizeBytes;
        this.fileType = fileType;
        this.deadline = deadline;
        this.extendedDeadline = extendedDeadline;
        this.maxMarks = maxMarks;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Timestamp getDeadline() {
        return deadline;
    }

    public void setDeadline(Timestamp deadline) {
        this.deadline = deadline;
    }

    public Timestamp getExtendedDeadline() {
        return extendedDeadline;
    }

    public void setExtendedDeadline(Timestamp extendedDeadline) {
        this.extendedDeadline = extendedDeadline;
    }

    public int getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(int maxMarks) {
        this.maxMarks = maxMarks;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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

    /**
     * Checks if the assignment is closed based on current time and deadline.
     * Deadline or Extended Deadline (if present) overrides everything.
     */
    public boolean isClosedByDeadline() {
        Timestamp activeDeadline = (extendedDeadline != null) ? extendedDeadline : deadline;
        return activeDeadline != null && new java.util.Date().after(activeDeadline);
    }

    /**
     * Gets the current active deadline (uses extended deadline if available).
     */
    public Timestamp getActiveDeadline() {
        return (extendedDeadline != null) ? extendedDeadline : deadline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assignment that = (Assignment) o;
        return assignmentId == that.assignmentId && 
               classId == that.classId && 
               teacherId == that.teacherId && 
               maxMarks == that.maxMarks && 
               Objects.equals(title, that.title) && 
               Objects.equals(description, that.description) && 
               Objects.equals(fileName, that.fileName) && 
               Objects.equals(filePath, that.filePath) && 
               Objects.equals(fileSizeBytes, that.fileSizeBytes) && 
               Objects.equals(fileType, that.fileType) && 
               Objects.equals(deadline, that.deadline) && 
               Objects.equals(extendedDeadline, that.extendedDeadline) && 
               status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId, title, description, classId, teacherId, 
                            fileName, filePath, fileSizeBytes, fileType, 
                            deadline, extendedDeadline, maxMarks, status);
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "assignmentId=" + assignmentId +
                ", title='" + title + '\'' +
                ", classId=" + classId +
                ", teacherId=" + teacherId +
                ", fileName='" + fileName + '\'' +
                ", deadline=" + deadline +
                ", extendedDeadline=" + extendedDeadline +
                ", maxMarks=" + maxMarks +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
