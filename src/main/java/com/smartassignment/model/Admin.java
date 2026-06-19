package com.smartassignment.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Model class representing a System Administrator.
 * Administrators manage teachers, students, classrooms, and system configurations.
 */
public class Admin implements Serializable {
    private static final long serialVersionUID = 1L;

    private int adminId;
    private String fullName;
    private String email;
    private String passwordHash;
    private String phone;
    private boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Default Constructor
    public Admin() {
        this.isActive = true;
    }

    // Constructor without ID (for creation)
    public Admin(String fullName, String email, String passwordHash, String phone, boolean isActive) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.isActive = isActive;
    }

    // Full Constructor
    public Admin(int adminId, String fullName, String email, String passwordHash, String phone, 
                 boolean isActive, Timestamp createdAt, Timestamp updatedAt) {
        this.adminId = adminId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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
        Admin admin = (Admin) o;
        return adminId == admin.adminId && 
               isActive == admin.isActive && 
               Objects.equals(fullName, admin.fullName) && 
               Objects.equals(email, admin.email) && 
               Objects.equals(passwordHash, admin.passwordHash) && 
               Objects.equals(phone, admin.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adminId, fullName, email, passwordHash, phone, isActive);
    }

    @Override
    public String toString() {
        return "Admin{" +
                "adminId=" + adminId +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
