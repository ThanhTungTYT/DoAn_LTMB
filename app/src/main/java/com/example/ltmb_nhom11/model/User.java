package com.example.ltmb_nhom11.model;

public class User {
    private String uid;
    private String fullName;
    private String phone;
    private String email;
    private String role; // "user" hoặc "doctor"
    private boolean verified;
    private long createdAt;

    public User() {} // cần constructor rỗng cho Firestore

    public User(String uid, String fullName, String phone, String email, String role) {
        this.uid = uid;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.verified = false;
        this.createdAt = System.currentTimeMillis();
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}