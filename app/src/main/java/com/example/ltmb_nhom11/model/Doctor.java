package com.example.ltmb_nhom11.model;

public class Doctor extends User{
    private String name;
    private String dept;
    private String status;
    private String timeSlot;
    private String avatarUrl;

    public Doctor(String name, String dept, String status) {
        this.name = name;
        this.dept = dept;
        this.status = status;
        this.timeSlot = "09:00 - 11:30";
    }

    public Doctor(String name, String dept, String status, String timeSlot) {
        this.name = name;
        this.dept = dept;
        this.status = status;
        this.timeSlot = timeSlot;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
