package com.hoanglong171.movies.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoanglong171.movies.R;
import com.hoanglong171.movies.adapters.UserAdapter;
import com.hoanglong171.movies.models.User;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersFragment extends Fragment implements UserAdapter.OnUserActionListener {

    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private List<User> originalUserList = new ArrayList<>(); // Danh sách gốc để lọc
    private DatabaseReference usersRef;
    private ProgressBar progressBarUsers;
    private TextView tvNoUsers;
    private SearchView searchViewUsers;
    private static final String TAG = "ManageUsersFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_users, container, false);

        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers);
        progressBarUsers = view.findViewById(R.id.progressBarUsers);
        searchViewUsers = view.findViewById(R.id.searchViewUsers);

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter(userList, this);
        recyclerViewUsers.setAdapter(userAdapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        loadUsers();

        // Thiết lập sự kiện tìm kiếm
        setupSearchView();

        return view;
    }

    private void setupSearchView() {
        searchViewUsers.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });
    }

    private void filterUsers(String query) {
        userList.clear();
        if (query.isEmpty()) {
            userList.addAll(originalUserList); // Nếu không có query, hiển thị toàn bộ danh sách gốc
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (User user : originalUserList) {
                if (user.getName().toLowerCase().contains(lowerCaseQuery) ||
                        user.getEmail().toLowerCase().contains(lowerCaseQuery)) {
                    userList.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged();
        if (userList.isEmpty()) {
            Toast.makeText(getContext(), "No users found matching your search", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUsers() {
        progressBarUsers.setVisibility(View.VISIBLE);

        // Kiểm tra trạng thái đăng nhập
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "User not logged in");
            progressBarUsers.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Please log in to view users", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra role của người dùng
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Checking role for UID: " + currentUserUid);
        usersRef.child(currentUserUid).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.getValue(String.class);
                Log.d(TAG, "User role: " + role);
                if (role != null && role.equals("admin")) {
                    // Người dùng là admin, tiếp tục tải danh sách người dùng
                    loadAllUsers();
                } else {
                    Log.d(TAG, "User is not admin");
                    progressBarUsers.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "You do not have permission to view users. Please log in as an admin.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking role: " + error.getMessage());
                progressBarUsers.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error checking your role: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllUsers() {
        Log.d(TAG, "Loading all users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                originalUserList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setUid(userSnapshot.getKey());
                        userList.add(user);
                        originalUserList.add(user); // Lưu vào danh sách gốc
                        Log.d(TAG, "Added user: " + user.getName() + " (" + user.getUid() + ")");
                    }
                }
                userAdapter.notifyDataSetChanged();
                progressBarUsers.setVisibility(View.GONE);
                if (userList.isEmpty()) {
                    Toast.makeText(getContext(), "No users found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading users: " + error.getMessage());
                progressBarUsers.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error loading users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditUser(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit User");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_user, null);
        EditText editTextName = dialogView.findViewById(R.id.editTextName);
        EditText editTextEmail = dialogView.findViewById(R.id.editTextEmail);
        EditText editTextDateOfBirth = dialogView.findViewById(R.id.editTextDateOfBirth);
        EditText editTextCity = dialogView.findViewById(R.id.editTextCity);
        EditText editTextRole = dialogView.findViewById(R.id.editTextRole);

        editTextName.setText(user.getName());
        editTextEmail.setText(user.getEmail());
        editTextDateOfBirth.setText(user.getDateOfBirth());
        editTextCity.setText(user.getCity());
        editTextRole.setText(user.getRole());

        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = editTextName.getText().toString().trim();
            String newEmail = editTextEmail.getText().toString().trim();
            String newDateOfBirth = editTextDateOfBirth.getText().toString().trim();
            String newCity = editTextCity.getText().toString().trim();
            String newRole = editTextRole.getText().toString().trim();

            if (newName.isEmpty() || newEmail.isEmpty() || newDateOfBirth.isEmpty() || newCity.isEmpty() || newRole.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            user.setName(newName);
            user.setEmail(newEmail);
            user.setDateOfBirth(newDateOfBirth);
            user.setCity(newCity);
            user.setRole(newRole);

            usersRef.child(user.getUid()).setValue(user)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "User updated successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error updating user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onDeleteUser(User user) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    usersRef.child(user.getUid()).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "User deleted successfully", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Error deleting user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}