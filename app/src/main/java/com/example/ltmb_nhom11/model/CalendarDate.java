package com.example.ltmb_nhom11.model;

/**
 * Model cho một ngày trong dải lịch chọn ngày khám.
 */
public class CalendarDate {
    private final String dayName;
    private final String dayValue;
    private final boolean isSelected;

    public CalendarDate(String dayName, String dayValue, boolean isSelected) {
        this.dayName = dayName;
        this.dayValue = dayValue;
        this.isSelected = isSelected;
    }

    public String getDayName() {
        return dayName;
    }

    public String getDayValue() {
        return dayValue;
    }

    public boolean isSelected() {
        return isSelected;
    }
}
