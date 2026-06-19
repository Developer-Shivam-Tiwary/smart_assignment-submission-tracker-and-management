package com.smartassignment.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Model class representing a Student account.
 * Students belong to a specific classroom and can submit/view assignments, track feedback, and grades.
 */
public class Student implements Serializable {
    private static final long serialVersionUID = 1L;

    private int studentId;
    private String fullName;
    private String email;
    private String passwordHash;
    private String rollNumber;
    private String phone;
    private int classId; // Reference to Class ID
    private boolean isActive;
    private int createdBy; // Reference to Admin ID
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Default Constructor
    public Student() {
        this.isActive = true;
    }

    // Constructor without ID (for creation)
    public Student(String fullName, String email, String passwordHash, String rollNumber, 
                   String phone, int classId, boolean isActive, int createdBy) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rollNumber = rollNumber;
        this.phone = phone;
        this.classId = classId;
        this.isActive = isActive;
        this.createdBy = createdBy;
    }

    // Full Constructor
    public Student(int studentId, String fullName, String email, String passwordHash, String rollNumber, 
                   String phone, int classId, boolean isActive, int createdBy, 
                   Timestamp createdAt, Timestamp updatedAt) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rollNumber = rollNumber;
        this.phone = phone;
        this.classId = classId;
        this.isActive = isActive;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
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

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
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
        Student student = (Student) o;
        return studentId == student.studentId && 
               classId == student.classId && 
               isActive == student.isActive && 
               createdBy == student.createdBy && 
               Objects.equals(fullName, student.fullName) && 
               Objects.equals(email, student.email) && 
               Objects.equals(passwordHash, student.passwordHash) && 
               Objects.equals(rollNumber, student.rollNumber) && 
               Objects.equals(phone, student.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, fullName, email, passwordHash, rollNumber, phone, classId, isActive, createdBy);
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", rollNumber='" + rollNumber + '\'' +
                ", phone='" + phone + '\'' +
                ", classId=" + classId +
                ", isActive=" + isActive +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
