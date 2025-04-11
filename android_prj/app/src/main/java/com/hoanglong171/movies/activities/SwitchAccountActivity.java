package com.hoanglong171.movies.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.hoanglong171.movies.R;

public class SwitchAccountActivity extends AppCompatActivity {

    private TextView tvAccount1, tvAccount2;
    private Button btnLogout;
    private FirebaseAuth auth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_account);

        // Ánh xạ các thành phần
        tvAccount1 = findViewById(R.id.tv_account_1);
        tvAccount2 = findViewById(R.id.tv_account_2);
        btnLogout = findViewById(R.id.btn_logout);

        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);

        // Hiển thị thông tin tài khoản đã lưu
        loadSavedAccounts();

        // Xử lý sự kiện click vào tài khoản
        tvAccount1.setOnClickListener(v -> loginWithAccount(1));
        tvAccount2.setOnClickListener(v -> loginWithAccount(2));

        // Xử lý sự kiện đăng xuất
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            sharedPreferences.edit().clear().apply(); // Xóa tài khoản đã lưu
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SwitchAccountActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void loadSavedAccounts() {
        String email1 = sharedPreferences.getString("email_1", null);
        String email2 = sharedPreferences.getString("email_2", null);

        tvAccount1.setText(email1 != null ? "Tài khoản 1: " + email1 : "Tài khoản 1: Chưa lưu");
        tvAccount2.setText(email2 != null ? "Tài khoản 2: " + email2 : "Tài khoản 2: Chưa lưu");
    }

    private void loginWithAccount(int accountNumber) {
        String emailKey = "email_" + accountNumber;
        String passwordKey = "password_" + accountNumber;

        String email = sharedPreferences.getString(emailKey, null);
        String password = sharedPreferences.getString(passwordKey, null);

        if (email == null || password == null) {
            Toast.makeText(this, "Tài khoản chưa được lưu", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SwitchAccountActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}