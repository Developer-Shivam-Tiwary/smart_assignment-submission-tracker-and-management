package com.smartassignment.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Model class representing a Teacher account.
 * Teachers can create assignments, view submissions, add grades/marks, and provide feedback.
 */
public class Teacher implements Serializable {
    private static final long serialVersionUID = 1L;

    private int teacherId;
    private String fullName;
    private String email;
    private String passwordHash;
    private String phone;
    private String subject;
    private String employeeCode;
    private boolean isActive;
    private int createdBy; // Reference to Admin ID
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Default Constructor
    public Teacher() {
        this.isActive = true;
    }

    // Constructor without ID (for creation)
    public Teacher(String fullName, String email, String passwordHash, String phone, 
                   String subject, String employeeCode, boolean isActive, int createdBy) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.subject = subject;
        this.employeeCode = employeeCode;
        this.isActive = isActive;
        this.createdBy = createdBy;
    }

    // Full Constructor
    public Teacher(int teacherId, String fullName, String email, String passwordHash, String phone, 
                   String subject, String employeeCode, boolean isActive, int createdBy, 
                   Timestamp createdAt, Timestamp updatedAt) {
        this.teacherId = teacherId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.subject = subject;
        this.employeeCode = employeeCode;
        this.isActive = isActive;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
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
        Teacher teacher = (Teacher) o;
        return teacherId == teacher.teacherId && 
               isActive == teacher.isActive && 
               createdBy == teacher.createdBy && 
               Objects.equals(fullName, teacher.fullName) && 
               Objects.equals(email, teacher.email) && 
               Objects.equals(passwordHash, teacher.passwordHash) && 
               Objects.equals(phone, teacher.phone) && 
               Objects.equals(subject, teacher.subject) && 
               Objects.equals(employeeCode, teacher.employeeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teacherId, fullName, email, passwordHash, phone, subject, employeeCode, isActive, createdBy);
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "teacherId=" + teacherId +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", subject='" + subject + '\'' +
                ", employeeCode='" + employeeCode + '\'' +
                ", isActive=" + isActive +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
