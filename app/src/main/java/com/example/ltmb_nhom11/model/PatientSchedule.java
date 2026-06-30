package com.example.ltmb_nhom11.model;
public class PatientSchedule {
    private String id;
    private String time;
    private String ampm;
    private String name;
    private String detail;
    private String status;
    private int statusBgColor;
    private int statusTextColor;

    public PatientSchedule() {}

    public PatientSchedule(String id, String time, String ampm, String name, String detail,
                           String status, int statusBgColor, int statusTextColor) {
        this.id = id;
        this.time = time;
        this.ampm = ampm;
        this.name = name;
        this.detail = detail;
        this.status = status;
        this.statusBgColor = statusBgColor;
        this.statusTextColor = statusTextColor;
    }

    public String getId() { return id; }
    public String getTime() { return time; }
    public String getAmpm() { return ampm; }
    public String getName() { return name; }
    public String getDetail() { return detail; }
    public String getStatus() { return status; }
    public int getStatusBgColor() { return statusBgColor; }
    public int getStatusTextColor() { return statusTextColor; }
}
