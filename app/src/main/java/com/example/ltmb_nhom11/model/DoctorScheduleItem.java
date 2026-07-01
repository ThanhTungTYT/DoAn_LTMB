package com.example.ltmb_nhom11.model;

public class DoctorScheduleItem {
    private String uid;
    private String fullName;
    private String dept;
    private String status;
    private String avatarUrl;
    private String timeSlot;
    private int appointmentsToday;
    private int totalSlots;
    private String leaveNote;

    public DoctorScheduleItem() {}

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public int getAppointmentsToday() { return appointmentsToday; }
    public void setAppointmentsToday(int appointmentsToday) { this.appointmentsToday = appointmentsToday; }
    public int getTotalSlots() { return totalSlots; }
    public void setTotalSlots(int totalSlots) { this.totalSlots = totalSlots; }
    public String getLeaveNote() { return leaveNote; }
    public void setLeaveNote(String leaveNote) { this.leaveNote = leaveNote; }

    public int getProgressPercent() {
        if (totalSlots <= 0) return 0;
        return Math.min(100, (appointmentsToday * 100) / totalSlots);
    }
}