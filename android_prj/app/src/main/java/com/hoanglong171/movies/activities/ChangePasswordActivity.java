package com.hoanglong171.movies.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.hoanglong171.movies.R;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText editOldPassword, editNewPassword, editConfirmPassword;
    private Button btnChangePassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Ánh xạ các thành phần
        editOldPassword = findViewById(R.id.edit_old_password);
        editNewPassword = findViewById(R.id.edit_new_password);
        editConfirmPassword = findViewById(R.id.edit_confirm_password);
        btnChangePassword = findViewById(R.id.btn_change_password);

        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Xử lý sự kiện đổi mật khẩu
        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String oldPassword = editOldPassword.getText().toString().trim();
        String newPassword = editNewPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        // Kiểm tra dữ liệu đầu vào
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Lỗi: Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Xác thực lại người dùng với mật khẩu cũ
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Cập nhật mật khẩu mới
                user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(this, "Đổi mật khẩu thành công, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        startActivity(new Intent(ChangePasswordActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Lỗi đổi mật khẩu: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}