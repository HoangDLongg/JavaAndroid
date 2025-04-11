package com.hoanglong171.movies.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoanglong171.movies.R;

public class ProfileActivity extends AppCompatActivity {
    private TextView nameText, emailText, dobText, cityText;
    private ImageView avatarImage;
    private Button btnEditProfile, btnChangePassword, btnSwitchAccount;
    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ánh xạ các thành phần
        nameText = findViewById(R.id.name_text);
        emailText = findViewById(R.id.email_text);
        dobText = findViewById(R.id.dob_text);
        cityText = findViewById(R.id.city_text);
        avatarImage = findViewById(R.id.avatar_image);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnSwitchAccount = findViewById(R.id.btn_switch_account);

        // Kiểm tra kết nối Firebase
        checkFirebaseConnection();

        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId != null) {
            Log.d("FirebaseUID", "User ID: " + userId);

            // Kết nối Firebase Realtime Database
            userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

            // Lấy thông tin người dùng
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Log.d("FirebaseData", "Dữ liệu nhận được: " + snapshot.getValue().toString());

                        String name = snapshot.child("name").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String dob = snapshot.child("dateOfBirth").getValue(String.class);
                        String city = snapshot.child("city").getValue(String.class);
                        String avatarBase64 = snapshot.child("avatarBase64").getValue(String.class);

                        // Cập nhật giao diện
                        nameText.setText("Tên: " + (name != null ? name : "N/A"));
                        emailText.setText("Email: " + (email != null ? email : "N/A"));
                        dobText.setText("Ngày sinh: " + (dob != null ? dob : "N/A"));
                        cityText.setText("Thành phố: " + (city != null ? city : "N/A"));

                        // Hiển thị avatar nếu có
                        if (avatarBase64 != null) {
                            try {
                                byte[] decodedString = android.util.Base64.decode(avatarBase64, android.util.Base64.DEFAULT);
                                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                avatarImage.setImageBitmap(decodedBitmap);
                            } catch (Exception e) {
                                Log.e("AvatarError", "Lỗi hiển thị avatar: " + e.getMessage());
                                avatarImage.setImageResource(R.drawable.profile);
                            }
                        }
                    } else {
                        Log.e("FirebaseData", "Không tìm thấy dữ liệu!");
                        Toast.makeText(ProfileActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("FirebaseData", "Lỗi Firebase: " + error.getMessage());
                    Toast.makeText(ProfileActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // Xử lý sự kiện click cho các nút
            btnEditProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
                }
            });

            btnChangePassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
                }
            });

            btnSwitchAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ProfileActivity.this, SwitchAccountActivity.class));
                }
            });

        } else {
            Toast.makeText(this, "Lỗi: Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void checkFirebaseConnection() {
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                Log.d("FirebaseConnection", "Trạng thái: " + connected);
                if (!connected) {
                    Toast.makeText(ProfileActivity.this, "Không có kết nối đến Firebase!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseConnection", "Lỗi: " + error.getMessage());
            }
        });
    }
}