package com.smartassignment.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Model class representing a Student Submission for an Assignment.
 */
public class Submission implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status {
        PENDING,
        SUBMITTED,
        LATE,
        MISSING
    }

    private int submissionId;
    private int assignmentId;
    private int studentId;
    private String fileName;
    private String filePath;
    private long fileSizeBytes;
    private String fileType;
    private Timestamp submittedAt;
    private Timestamp resubmittedAt; // Null if not resubmitted
    private int resubmitCount;
    private Status status;
    private BigDecimal marks; // Null if not graded yet
    private String feedback;  // Null if no feedback
    private Timestamp marksUpdatedAt;
    private Timestamp feedbackUpdatedAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Default Constructor
    public Submission() {
        this.resubmitCount = 0;
        this.status = Status.SUBMITTED;
    }

    // Constructor without ID (for creation)
    public Submission(int assignmentId, int studentId, String fileName, String filePath, 
                      long fileSizeBytes, String fileType, Timestamp submittedAt, 
                      Timestamp resubmittedAt, int resubmitCount, Status status, 
                      BigDecimal marks, String feedback) {
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSizeBytes = fileSizeBytes;
        this.fileType = fileType;
        this.submittedAt = submittedAt;
        this.resubmittedAt = resubmittedAt;
        this.resubmitCount = resubmitCount;
        this.status = status;
        this.marks = marks;
        this.feedback = feedback;
    }

    // Full Constructor
    public Submission(int submissionId, int assignmentId, int studentId, String fileName, 
                      String filePath, long fileSizeBytes, String fileType, Timestamp submittedAt, 
                      Timestamp resubmittedAt, int resubmitCount, Status status, BigDecimal marks, 
                      String feedback, Timestamp marksUpdatedAt, Timestamp feedbackUpdatedAt, 
                      Timestamp createdAt, Timestamp updatedAt) {
        this.submissionId = submissionId;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSizeBytes = fileSizeBytes;
        this.fileType = fileType;
        this.submittedAt = submittedAt;
        this.resubmittedAt = resubmittedAt;
        this.resubmitCount = resubmitCount;
        this.status = status;
        this.marks = marks;
        this.feedback = feedback;
        this.marksUpdatedAt = marksUpdatedAt;
        this.feedbackUpdatedAt = feedbackUpdatedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(int submissionId) {
        this.submissionId = submissionId;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
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

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Timestamp getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Timestamp submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Timestamp getResubmittedAt() {
        return resubmittedAt;
    }

    public void setResubmittedAt(Timestamp resubmittedAt) {
        this.resubmittedAt = resubmittedAt;
    }

    public int getResubmitCount() {
        return resubmitCount;
    }

    public void setResubmitCount(int resubmitCount) {
        this.resubmitCount = resubmitCount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public BigDecimal getMarks() {
        return marks;
    }

    public void setMarks(BigDecimal marks) {
        this.marks = marks;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Timestamp getMarksUpdatedAt() {
        return marksUpdatedAt;
    }

    public void setMarksUpdatedAt(Timestamp marksUpdatedAt) {
        this.marksUpdatedAt = marksUpdatedAt;
    }

    public Timestamp getFeedbackUpdatedAt() {
        return feedbackUpdatedAt;
    }

    public void setFeedbackUpdatedAt(Timestamp feedbackUpdatedAt) {
        this.feedbackUpdatedAt = feedbackUpdatedAt;
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
     * Business rule: Checks if a re-upload is allowed.
     * Re-upload is allowed only within 30 minutes of the first submission, and before the deadline.
     * Deadline overrides the 30-minute window.
     * 
     * @param activeDeadline The active deadline for the assignment (extended or original).
     * @return true if re-upload is permitted, false otherwise.
     */
    public boolean isReUploadAllowed(Timestamp activeDeadline) {
        long currentTimeMillis = System.currentTimeMillis();
        
        // 1. Deadline Check: If current time is after deadline, re-upload is NOT allowed.
        if (activeDeadline != null && currentTimeMillis > activeDeadline.getTime()) {
            return false;
        }

        // 2. 30-minute window check from original submission time (submittedAt)
        if (submittedAt != null) {
            long submittedTimeMillis = submittedAt.getTime();
            long thirtyMinutesInMillis = 30 * 60 * 1000L;
            return (currentTimeMillis - submittedTimeMillis) <= thirtyMinutesInMillis;
        }
        
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Submission that = (Submission) o;
        return submissionId == that.submissionId && 
               assignmentId == that.assignmentId && 
               studentId == that.studentId && 
               fileSizeBytes == that.fileSizeBytes && 
               resubmitCount == that.resubmitCount && 
               Objects.equals(fileName, that.fileName) && 
               Objects.equals(filePath, that.filePath) && 
               Objects.equals(fileType, that.fileType) && 
               Objects.equals(submittedAt, that.submittedAt) && 
               Objects.equals(resubmittedAt, that.resubmittedAt) && 
               status == that.status && 
               Objects.equals(marks, that.marks) && 
               Objects.equals(feedback, that.feedback);
    }

    @Override
    public int hashCode() {
        return Objects.hash(submissionId, assignmentId, studentId, fileName, filePath, 
                            fileSizeBytes, fileType, submittedAt, resubmittedAt, 
                            resubmitCount, status, marks, feedback);
    }

    @Override
    public String toString() {
        return "Submission{" +
                "submissionId=" + submissionId +
                ", assignmentId=" + assignmentId +
                ", studentId=" + studentId +
                ", fileName='" + fileName + '\'' +
                ", submittedAt=" + submittedAt +
                ", resubmittedAt=" + resubmittedAt +
                ", resubmitCount=" + resubmitCount +
                ", status=" + status +
                ", marks=" + marks +
                '}';
    }
}
