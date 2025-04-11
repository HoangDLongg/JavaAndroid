package com.hoanglong171.movies.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.InputStream;
import java.util.Calendar;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editName, editDob, editCity;
    private ImageView avatarImage;
    private Button btnSelectAvatar, btnSave;
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private Uri avatarUri;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Ánh xạ các thành phần
        editName = findViewById(R.id.edit_name);
        editDob = findViewById(R.id.edit_dob);
        editCity = findViewById(R.id.edit_city);
        avatarImage = findViewById(R.id.avatar_image);
        btnSelectAvatar = findViewById(R.id.btn_select_avatar);
        btnSave = findViewById(R.id.btn_save);

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Lỗi: Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

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

        // Xử lý sự kiện chọn ngày sinh
        editDob.setOnClickListener(v -> showDatePickerDialog());

        // Lấy thông tin người dùng hiện tại
        loadUserData();

        // Xử lý sự kiện lưu thay đổi
        btnSave.setOnClickListener(v -> saveUserData());
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String dob = snapshot.child("dateOfBirth").getValue(String.class);
                    String city = snapshot.child("city").getValue(String.class);
                    String avatarBase64 = snapshot.child("avatarBase64").getValue(String.class);

                    editName.setText(name != null ? name : "");
                    editDob.setText(dob != null ? dob : "");
                    editCity.setText(city != null ? city : "");

                    if (avatarBase64 != null) {
                        try {
                            byte[] decodedString = android.util.Base64.decode(avatarBase64, android.util.Base64.DEFAULT);
                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            avatarImage.setImageBitmap(decodedBitmap);
                        } catch (Exception e) {
                            avatarImage.setImageResource(R.drawable.profile);
                        }
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                    editDob.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void saveUserData() {
        String name = editName.getText().toString().trim();
        String dob = editDob.getText().toString().trim();
        String city = editCity.getText().toString().trim();

        if (name.isEmpty() || dob.isEmpty() || city.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cập nhật thông tin người dùng
        userRef.child("name").setValue(name);
        userRef.child("dateOfBirth").setValue(dob);
        userRef.child("city").setValue(city);

        // Cập nhật avatar nếu có
        if (avatarUri != null) {
            String avatarBase64 = convertImageToBase64(avatarUri);
            if (avatarBase64 != null) {
                userRef.child("avatarBase64").setValue(avatarBase64);
            } else {
                Toast.makeText(this, "Không thể lưu avatar", Toast.LENGTH_SHORT).show();
            }
        }

        Toast.makeText(this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String convertImageToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();
            return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}