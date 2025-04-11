package com.hoanglong171.movies.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoanglong171.movies.R;
import com.hoanglong171.movies.adapters.VideoAdapter;
import com.hoanglong171.movies.models.Movie;

import java.util.ArrayList;
import java.util.List;

public class ManageVideosFragment extends Fragment {

    private RecyclerView recyclerViewVideos;
    private VideoAdapter videoAdapter;
    private List<Movie> videoList = new ArrayList<>();
    private DatabaseReference moviesRef;
    private ProgressBar progressBarVideos;
    private Button btnAddVideo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_videos, container, false);

        recyclerViewVideos = view.findViewById(R.id.recyclerViewVideos);
        progressBarVideos = view.findViewById(R.id.progressBarVideos);
        btnAddVideo = view.findViewById(R.id.btnAddVideo);

        recyclerViewVideos.setLayoutManager(new LinearLayoutManager(getContext()));
        // Sửa lại cách khởi tạo VideoAdapter
        videoAdapter = new VideoAdapter(videoList, new VideoAdapter.OnVideoActionListener() {
            @Override
            public void onEdit(Movie movie) {
                showEditVideoDialog(movie);
            }

            @Override
            public void onDelete(Movie movie) {
                deleteVideo(movie);
            }
        });
        recyclerViewVideos.setAdapter(videoAdapter);

        moviesRef = FirebaseDatabase.getInstance().getReference("movies");
        loadVideos();

        btnAddVideo.setOnClickListener(v -> showAddVideoDialog());

        return view;
    }

    private void loadVideos() {
        progressBarVideos.setVisibility(View.VISIBLE);
        moviesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                videoList.clear();
                for (DataSnapshot videoSnapshot : snapshot.getChildren()) {
                    Movie movie = videoSnapshot.getValue(Movie.class);
                    if (movie != null) {
                        movie.setId(videoSnapshot.getKey());
                        videoList.add(movie);
                    }
                }
                videoAdapter.notifyDataSetChanged();
                progressBarVideos.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi tải video: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBarVideos.setVisibility(View.GONE);
            }
        });
    }

    private void showAddVideoDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_add_edit_video);

        EditText etVideoId = dialog.findViewById(R.id.etVideoId); // Thêm EditText cho ID
        EditText etVideoTitle = dialog.findViewById(R.id.etVideoTitle);
        EditText etVideoDescription = dialog.findViewById(R.id.etVideoDescription);
        EditText etVideoUrl = dialog.findViewById(R.id.etVideoUrl);
        EditText etThumbnailUrl = dialog.findViewById(R.id.etThumbnailUrl);
        Button btnSaveVideo = dialog.findViewById(R.id.btnSaveVideo);
        Button btnCancelVideo = dialog.findViewById(R.id.btnCancelVideo);

        // Đặt gợi ý cho EditText ID
        etVideoId.setHint("Nhập ID video (ví dụ: lilo_stitch_trailer)");

        btnSaveVideo.setOnClickListener(v -> {
            String videoId = etVideoId.getText().toString().trim();
            String title = etVideoTitle.getText().toString().trim();
            String description = etVideoDescription.getText().toString().trim();
            String videoUrl = etVideoUrl.getText().toString().trim();
            String thumbnailUrl = etThumbnailUrl.getText().toString().trim();

            if (videoId.isEmpty() || title.isEmpty() || description.isEmpty() || videoUrl.isEmpty() || thumbnailUrl.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin, bao gồm ID", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra xem ID đã tồn tại chưa
            moviesRef.child(videoId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(getContext(), "ID đã tồn tại, vui lòng chọn ID khác", Toast.LENGTH_SHORT).show();
                    } else {
                        Movie newMovie = new Movie();
                        newMovie.setTitle(title);
                        newMovie.setDescription(description);
                        newMovie.setVideoUrl(videoUrl);
                        newMovie.setThumbnailUrl(thumbnailUrl);

                        moviesRef.child(videoId).setValue(newMovie)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Thêm video thành công", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Lỗi kiểm tra ID: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnCancelVideo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showEditVideoDialog(Movie movie) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_add_edit_video);

        EditText etVideoId = dialog.findViewById(R.id.etVideoId); // EditText cho ID
        EditText etVideoTitle = dialog.findViewById(R.id.etVideoTitle);
        EditText etVideoDescription = dialog.findViewById(R.id.etVideoDescription);
        EditText etVideoUrl = dialog.findViewById(R.id.etVideoUrl);
        EditText etThumbnailUrl = dialog.findViewById(R.id.etThumbnailUrl);
        Button btnSaveVideo = dialog.findViewById(R.id.btnSaveVideo);
        Button btnCancelVideo = dialog.findViewById(R.id.btnCancelVideo);

        // Điền thông tin hiện tại
        etVideoId.setText(movie.getId());
        etVideoTitle.setText(movie.getTitle());
        etVideoDescription.setText(movie.getDescription());
        etVideoUrl.setText(movie.getVideoUrl());
        etThumbnailUrl.setText(movie.getThumbnailUrl());

        // Disable chỉnh sửa ID khi edit để tránh xung đột
        etVideoId.setEnabled(false);

        btnSaveVideo.setOnClickListener(v -> {
            String title = etVideoTitle.getText().toString().trim();
            String description = etVideoDescription.getText().toString().trim();
            String videoUrl = etVideoUrl.getText().toString().trim();
            String thumbnailUrl = etThumbnailUrl.getText().toString().trim();

            if (title.isEmpty() || description.isEmpty() || videoUrl.isEmpty() || thumbnailUrl.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            movie.setTitle(title);
            movie.setDescription(description);
            movie.setVideoUrl(videoUrl);
            movie.setThumbnailUrl(thumbnailUrl);

            moviesRef.child(movie.getId()).setValue(movie)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Cập nhật video thành công", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        btnCancelVideo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void deleteVideo(Movie movie) {
        moviesRef.child(movie.getId()).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Xóa video thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}