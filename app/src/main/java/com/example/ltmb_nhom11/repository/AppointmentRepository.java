package com.example.ltmb_nhom11.repository;

import androidx.annotation.NonNull;

import com.example.ltmb_nhom11.model.Appointment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Truy cập collection "appointments" trên Firestore.
 * Mọi thao tác CRUD cho lịch hẹn nằm gọn ở đây (tách lớp dữ liệu).
 */
public class AppointmentRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION = "appointments";

    public interface OnDone {
        void onSuccess();
        void onError(Exception e);
    }

    public interface OnList {
        void onLoaded(List<Appointment> list);
        void onError(Exception e);
    }

    /** Tạo lịch hẹn mới. */
    public void create(Appointment a, OnDone cb) {
        db.collection(COLLECTION).add(a)
                .addOnSuccessListener(ref -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    /** Lấy tất cả lịch hẹn của 1 người dùng. */
    public void getByUser(String userId, OnList cb) {
        db.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Appointment> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        Appointment a = doc.toObject(Appointment.class);
                        a.setId(doc.getId());
                        list.add(a);
                    }
                    cb.onLoaded(list);
                })
                .addOnFailureListener(cb::onError);
    }

    /** Cập nhật trạng thái (vd hủy lịch: status = "cancelled"). */
    public void updateStatus(@NonNull String id, String status, OnDone cb) {
        db.collection(COLLECTION).document(id).update("status", status)
                .addOnSuccessListener(x -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }
}
