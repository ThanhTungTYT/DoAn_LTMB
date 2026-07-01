package com.example.ltmb_nhom11.model;

/**
 * Model một lịch hẹn khám. Dùng chung cho cả luồng đặt bác sĩ cá nhân (type="doctor")
 * và luồng gói khám combo (type="package").
 * Firestore yêu cầu có constructor rỗng + getter/setter công khai.
 */
public class Appointment {
    private String id;          // id document Firestore (gán sau khi đọc)
    private String userId;
    private String type;        // "doctor" | "package"
    private String doctorId;
    private String doctorName;
    private String packageId;
    private String packageName;
    private String date;        // vd "15/10/2023"
    private String time;        // vd "08:00"
    private long price;
    private String status;      // "upcoming" | "done" | "cancelled"
    private long createdAt;// thời điểm đặt lịch (millis)

    public Appointment() {}     // BẮT BUỘC cho Firestore

    public Appointment(String userId, String type, String doctorId, String doctorName,
                       String date, String time, long price, String status) {
        this.userId = userId;
        this.type = type;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.price = price;
        this.status = status;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
