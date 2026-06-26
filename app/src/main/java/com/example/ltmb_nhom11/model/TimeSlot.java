package com.example.ltmb_nhom11.model;

public class TimeSlot {

    private final String time;
    private boolean selected;

    public TimeSlot(String time) {
        this.time = time;
        this.selected = false;
    }

    public String getTime() {
        return time;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}