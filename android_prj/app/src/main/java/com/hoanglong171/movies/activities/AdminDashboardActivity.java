package com.hoanglong171.movies.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoanglong171.movies.R;
import com.hoanglong171.movies.fragment.ManageUsersFragment;
import com.hoanglong171.movies.fragment.ManageVideosFragment;

public class AdminDashboardActivity extends AppCompatActivity {

    private DatabaseReference usersRef;
    private static final String TAG = "AdminDashboard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Kiểm tra trạng thái đăng nhập
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "User not logged in, redirecting to LoginActivity");
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Kiểm tra role của người dùng
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Checking role for UID: " + currentUserUid);
        usersRef.child(currentUserUid).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String role = dataSnapshot.getValue(String.class);
                Log.d(TAG, "User role: " + role);
                if (role != null && role.equals("admin")) {
                    Log.d(TAG, "User is admin, setting up ViewPager");
                    setupViewPager();
                } else {
                    Log.d(TAG, "User is not admin, redirecting to MainActivity");
                    Toast.makeText(AdminDashboardActivity.this, "You do not have permission to access this page", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error checking role: " + databaseError.getMessage());
                Toast.makeText(AdminDashboardActivity.this, "Error checking role: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                // Không điều hướng về LoginActivity ngay lập tức, cho phép thử lại
            }
        });
    }

    private void setupViewPager() {
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        // Tạo adapter cho ViewPager2
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() {
                return 2; // 2 tab: Quản lý Video và Quản lý Người dùng
            }

            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return new ManageVideosFragment();
                    case 1:
                        return new ManageUsersFragment();
                    default:
                        return new ManageVideosFragment();
                }
            }
        });

        // Liên kết TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Quản lý Video");
                    break;
                case 1:
                    tab.setText("Quản lý Người dùng");
                    break;
            }
        }).attach();
    }
}