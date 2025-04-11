package com.hoanglong171.movies.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoanglong171.movies.R;
import com.hoanglong171.movies.models.User;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import androidx.annotation.NonNull;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText nameUser, email, pass1, pass2, dateOfBirth, city;
    private ImageView avatarImage, togglePass1, togglePass2;
    private Button btnRegister, btnSelectAvatar;
    private TextView tvLogin;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private DatabaseReference moviesRef;
    private DatabaseReference ratingsRef;
    private DatabaseReference favoritesRef;
    private Uri avatarUri;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private boolean isPass1Visible = false;
    private boolean isPass2Visible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo các view
        nameUser = findViewById(R.id.name_user);
        email = findViewById(R.id.emailInput);
        pass1 = findViewById(R.id.pass1);
        pass2 = findViewById(R.id.pass2);
        dateOfBirth = findViewById(R.id.date_of_birth);
        city = findViewById(R.id.city);
        avatarImage = findViewById(R.id.avatarImage);
        togglePass1 = findViewById(R.id.toggle_pass1);
        togglePass2 = findViewById(R.id.toggle_pass2);
        btnRegister = findViewById(R.id.startBtn);
        btnSelectAvatar = findViewById(R.id.selectAvatarBtn);
        tvLogin = findViewById(R.id.textView7);

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        moviesRef = FirebaseDatabase.getInstance().getReference("movies");
        ratingsRef = FirebaseDatabase.getInstance().getReference("ratings");
        favoritesRef = FirebaseDatabase.getInstance().getReference("favorites");

        // Khởi tạo launcher để chọn ảnh
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                avatarUri = uri;
                avatarImage.setImageURI(uri);
            }
        });

        // Xử lý sự kiện chọn ảnh
        btnSelectAvatar.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        avatarImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Xử lý sự kiện toggle mật khẩu
        togglePass1.setOnClickListener(v -> togglePasswordVisibility(pass1, togglePass1));
        togglePass2.setOnClickListener(v -> togglePasswordVisibility(pass2, togglePass2));

        // Xử lý sự kiện chọn ngày sinh
        dateOfBirth.setOnClickListener(v -> showDatePickerDialog());

        // Xử lý sự kiện đăng ký
        btnRegister.setOnClickListener(v -> registerUser());

        // Xử lý sự kiện click vào "Đã có tài khoản? Đăng nhập"
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageView toggleIcon) {
        if (editText.getId() == R.id.pass1) {
            isPass1Visible = !isPass1Visible;
            if (isPass1Visible) {
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggleIcon.setImageResource(R.drawable.visibility_off);
            } else {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggleIcon.setImageResource(R.drawable.visibility_on);
            }
        } else if (editText.getId() == R.id.pass2) {
            isPass2Visible = !isPass2Visible;
            if (isPass2Visible) {
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggleIcon.setImageResource(R.drawable.visibility_off);
            } else {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggleIcon.setImageResource(R.drawable.visibility_on);
            }
        }
        editText.setSelection(editText.getText().length());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    dateOfBirth.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void registerUser() {
        String name = nameUser.getText().toString().trim();
        String emailStr = email.getText().toString().trim();
        String password = pass1.getText().toString().trim();
        String confirmPassword = pass2.getText().toString().trim();
        String dob = dateOfBirth.getText().toString().trim();
        String cityStr = city.getText().toString().trim();

        // Kiểm tra dữ liệu đầu vào
        if (name.isEmpty() || emailStr.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || dob.isEmpty() || cityStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đăng ký tài khoản với Firebase Authentication
        auth.createUserWithEmailAndPassword(emailStr, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();
                        Log.d(TAG, "User registered with UID: " + userId);

                        // Tạo đối tượng User
                        User user = new User(userId, name, emailStr, dob, cityStr, null, "user");

                        // Chuyển avatar thành Base64 (nếu có)
                        if (avatarUri != null) {
                            String avatarBase64 = convertImageToBase64(avatarUri);
                            if (avatarBase64 != null) {
                                user.setAvatarBase64(avatarBase64);
                            } else {
                                Toast.makeText(this, "Không thể lưu avatar", Toast.LENGTH_SHORT).show();
                            }
                        }

                        // Lưu thông tin người dùng vào Firebase Realtime Database
                        usersRef.child(userId).setValue(user)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "User info saved for UID: " + userId);
                                    // Tự động thêm UID vào ratings và favorites
                                    autoAddUserToRatingsAndFavorites(userId);
                                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công, vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();

                                    // Chuyển sang LoginActivity
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error saving user info: " + e.getMessage());
                                    Toast.makeText(RegisterActivity.this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    // Xóa tài khoản nếu lưu dữ liệu thất bại
                                    auth.getCurrentUser().delete().addOnCompleteListener(deleteTask -> {
                                        if (deleteTask.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, "Đã xóa tài khoản do lỗi lưu dữ liệu", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Lỗi xóa tài khoản: " + deleteTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                });
                    } else {
                        Log.e(TAG, "Registration failed: " + task.getException().getMessage());
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void autoAddUserToRatingsAndFavorites(String userId) {
        // Lấy danh sách tất cả phim từ node movies
        moviesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    String movieId = movieSnapshot.getKey();

                    // Thêm UID vào ratings với giá trị mặc định 0.0
                    DatabaseReference movieRatingRef = ratingsRef.child(movieId).child(userId);
                    movieRatingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                movieRatingRef.setValue(0.0f)
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Added default rating for user " + userId + " in movie " + movieId))
                                        .addOnFailureListener(e -> Log.e(TAG, "Error adding default rating: " + e.getMessage()));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error checking rating for movie " + movieId + ": " + error.getMessage());
                        }
                    });

                    // Thêm UID vào favorites với giá trị mặc định false
                    DatabaseReference movieFavoriteRef = favoritesRef.child(userId).child(movieId);
                    movieFavoriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                movieFavoriteRef.setValue(false)
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Added default favorite for user " + userId + " in movie " + movieId))
                                        .addOnFailureListener(e -> Log.e(TAG, "Error adding default favorite: " + e.getMessage()));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error checking favorite for movie " + movieId + ": " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading movies: " + error.getMessage());
            }
        });
    }

    private String convertImageToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();
            return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}