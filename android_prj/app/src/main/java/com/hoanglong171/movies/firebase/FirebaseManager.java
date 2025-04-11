package com.hoanglong171.movies.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseManager {
    private FirebaseAuth mAuth;
    private static FirebaseManager instance;

    // Singleton pattern để đảm bảo chỉ có một instance
    public static FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    private FirebaseManager() {
        mAuth = FirebaseAuth.getInstance(); // Khởi tạo Firebase Authentication
    }
    public void loginUser(String email, String password, LoginCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            callback.onSuccess(user.getUid());
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Đăng nhập thất bại";
                        callback.onFailure(errorMessage);
                    }
                });
    }
    // Phương thức đăng ký tài khoản
    public void registerUser(String email, String password, RegisterCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            callback.onSuccess(user.getUid()); // Trả về UID khi thành công
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Đăng ký thất bại";
                        callback.onFailure(errorMessage); // Trả về lỗi nếu thất bại
                    }
                });
    }


    // Callback interface để xử lý kết quả đăng ký
    public interface RegisterCallback {
        void onSuccess(String uid);
        void onFailure(String errorMessage);
    }
    public interface LoginCallback {
        void onSuccess(String uid);
        void onFailure(String errorMessage);
    }
    // Lấy thông tin người dùng hiện tại
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    // Đăng xuất (nếu cần sau này)
    public void signOut() {
        mAuth.signOut();
    }
}