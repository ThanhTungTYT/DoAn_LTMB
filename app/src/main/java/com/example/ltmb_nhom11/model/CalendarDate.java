package com.example.ltmb_nhom11.model;

/**
 * Model một ngày trong dải lịch chọn ngày khám.
 * selectable = false cho Chủ nhật (không cho đặt).
 */
public class CalendarDate {
    private final String dayName;    // T2, T3, ... CN
    private final String dayValue;   // 8
    private final String fullDate;   // 08/07/2025
    private final boolean selectable;

    public CalendarDate(String dayName, String dayValue, String fullDate, boolean selectable) {
        this.dayName = dayName;
        this.dayValue = dayValue;
        this.fullDate = fullDate;
        this.selectable = selectable;
    }

    public String getDayName() { return dayName; }
    public String getDayValue() { return dayValue; }
    public String getFullDate() { return fullDate; }
    public boolean isSelectable() { return selectable; }
}
