package com.hoanglong171.movies.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoanglong171.movies.R;
import com.hoanglong171.movies.databinding.ActivityLoginBinding;
import com.hoanglong171.movies.firebase.FirebaseManager;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    private FirebaseManager firebaseManager;
    private DatabaseReference usersRef;
    private boolean isPasswordVisible = false;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Thiết lập giao diện tràn cạnh từ đầu
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // Khởi tạo View Binding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo FirebaseManager và DatabaseReference
        firebaseManager = FirebaseManager.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);

        // Xử lý sự kiện nút "Đăng Nhập"
        binding.startBtn.setOnClickListener(v -> loginAccount());

        // Xử lý sự kiện nhấn vào TextView "Sign in" để chuyển sang RegisterActivity
        binding.textView7.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Xử lý ẩn/hiện mật khẩu
        binding.pass.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (binding.pass.getRight() - binding.pass.getCompoundDrawables()[2].getBounds().width())) {
                    togglePasswordVisibility();
                    return true;
                }
            }
            return false;
        });

        // Xử lý WindowInsets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loginAccount() {
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.pass.getText().toString().trim();

        // Kiểm tra dữ liệu đầu vào
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi FirebaseManager để đăng nhập
        firebaseManager.loginUser(email, password, new FirebaseManager.LoginCallback() {
            @Override
            public void onSuccess(String uid) {
                // Lưu thông tin tài khoản vào SharedPreferences
                saveAccount(email, password);

                // Kiểm tra role của người dùng
                checkUserRole(uid);
            }

            @Override
            public void onFailure(String errorMessage) {
                // Xử lý lỗi cụ thể từ Firebase
                if (errorMessage.contains("The email address is badly formatted")) {
                    Toast.makeText(LoginActivity.this, "Email không đúng định dạng", Toast.LENGTH_LONG).show();
                } else if (errorMessage.contains("There is no user record corresponding to this identifier")) {
                    Toast.makeText(LoginActivity.this, "Email chưa được đăng ký", Toast.LENGTH_LONG).show();
                } else if (errorMessage.contains("The password is invalid")) {
                    Toast.makeText(LoginActivity.this, "Mật khẩu không đúng", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void checkUserRole(String uid) {
        usersRef.child(uid).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String role = dataSnapshot.getValue(String.class);
                if (role != null) {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent;
                    if (role.equals("admin")) {
                        intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                    } else {
                        intent = new Intent(LoginActivity.this, MainActivity.class);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Không tìm thấy vai trò người dùng", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Lỗi khi kiểm tra vai trò: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveAccount(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Kiểm tra tài khoản đã lưu
        String email1 = sharedPreferences.getString("email_1", null);
        String email2 = sharedPreferences.getString("email_2", null);

        if (email1 == null) {
            // Lưu vào slot 1
            editor.putString("email_1", email);
            editor.putString("password_1", password);
        } else if (email2 == null && !email1.equals(email)) {
            // Lưu vào slot 2 nếu slot 1 đã có và email khác
            editor.putString("email_2", email);
            editor.putString("password_2", password);
        } else if (email1.equals(email)) {
            // Cập nhật slot 1 nếu email đã tồn tại
            editor.putString("password_1", password);
        } else if (email2 != null && email2.equals(email)) {
            // Cập nhật slot 2 nếu email đã tồn tại
            editor.putString("password_2", password);
        } else {
            // Nếu cả 2 slot đều đầy và email không trùng, thay thế slot 2
            editor.putString("email_2", email);
            editor.putString("password_2", password);
        }

        editor.apply();
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            binding.pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            binding.pass.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, R.drawable.visibility_on, 0);
        } else {
            binding.pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            binding.pass.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, R.drawable.visibility_off, 0);
        }
        isPasswordVisible = !isPasswordVisible;
        binding.pass.setSelection(binding.pass.getText().length()); // Đặt con trỏ ở cuối
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }
}