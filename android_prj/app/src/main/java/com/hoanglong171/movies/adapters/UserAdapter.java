package com.hoanglong171.movies.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hoanglong171.movies.R;
import com.hoanglong171.movies.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private OnUserActionListener actionListener;

    public interface OnUserActionListener {
        void onEditUser(User user);
        void onDeleteUser(User user);
    }

    public UserAdapter(List<User> userList, OnUserActionListener actionListener) {
        this.userList = userList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvUserName.setText(user.getName());
        holder.tvUserEmail.setText(user.getEmail());
        holder.tvUserDateOfBirth.setText("DOB: " + user.getDateOfBirth());
        holder.tvUserCity.setText("City: " + user.getCity());
        holder.tvUserRole.setText("Role: " + user.getRole());

        holder.btnEdit.setOnClickListener(v -> actionListener.onEditUser(user));
        holder.btnDelete.setOnClickListener(v -> actionListener.onDeleteUser(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail, tvUserDateOfBirth, tvUserCity, tvUserRole;
        Button btnEdit, btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserDateOfBirth = itemView.findViewById(R.id.tvUserDateOfBirth);
            tvUserCity = itemView.findViewById(R.id.tvUserCity);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}