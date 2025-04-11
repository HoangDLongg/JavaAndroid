package com.hoanglong171.movies.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoanglong171.movies.R;
import com.hoanglong171.movies.adapters.MovieAdapter;
import com.hoanglong171.movies.models.Movie;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class FavoritesActivity extends AppCompatActivity {
    private static final String TAG = "FavoritesActivity";
    private RecyclerView recyclerViewFavorites;
    private MovieAdapter favoritesAdapter;
    private List<Movie> favoriteMovies = new ArrayList<>();
    private FirebaseAuth auth;
    private DatabaseReference moviesRef;
    private DatabaseReference favoritesRef;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorites);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ các thành phần
        recyclerViewFavorites = findViewById(R.id.recyclerViewFavorites);
        progressBar = findViewById(R.id.progressBar);

        // Cấu hình LayoutManager cho RecyclerView (dọc)
        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "User not logged in, redirecting to LoginActivity");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        String userId = auth.getCurrentUser().getUid();

        // Khởi tạo Adapter
        favoritesAdapter = new MovieAdapter(favoriteMovies, movie -> {
            openVideoPlayer(movie.getVideoUrl());
        }, (movieId, rating) -> submitRating(movieId, rating), (movieId, isFavorite) -> toggleFavorite(movieId, isFavorite));
        recyclerViewFavorites.setAdapter(favoritesAdapter);

        // Khởi tạo tham chiếu Firebase
        moviesRef = FirebaseDatabase.getInstance().getReference("movies");
        favoritesRef = FirebaseDatabase.getInstance().getReference("favorites").child(userId);

        // Tải danh sách phim yêu thích
        loadFavoriteMovies();
    }

    private void loadFavoriteMovies() {
        progressBar.setVisibility(View.VISIBLE);

        // Lấy danh sách phim yêu thích của người dùng
        favoritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteMovies.clear();
                List<String> favoriteMovieIds = new ArrayList<>();
                for (DataSnapshot favoriteSnapshot : snapshot.getChildren()) {
                    String movieId = favoriteSnapshot.getKey();
                    Boolean isFavorite = favoriteSnapshot.getValue(Boolean.class);
                    if (isFavorite != null && isFavorite) {
                        favoriteMovieIds.add(movieId);
                    }
                }

                if (favoriteMovieIds.isEmpty()) {
                    Log.w(TAG, "No favorite movies found");
                    progressBar.setVisibility(View.GONE);
                    favoritesAdapter.notifyDataSetChanged();
                    return;
                }

                // Lấy thông tin chi tiết của các phim yêu thích
                for (String movieId : favoriteMovieIds) {
                    moviesRef.child(movieId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot movieSnapshot) {
                            Movie movie = movieSnapshot.getValue(Movie.class);
                            if (movie != null) {
                                movie.setId(movieSnapshot.getKey());
                                movie.setFavorite(true); // Đã là phim yêu thích

                                // Lấy rating trung bình
                                DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("ratings").child(movie.getId());
                                ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot ratingSnapshot) {
                                        float totalRating = 0;
                                        int count = 0;
                                        for (DataSnapshot userRating : ratingSnapshot.getChildren()) {
                                            Float rating = userRating.getValue(Float.class);
                                            if (rating != null) {
                                                totalRating += rating;
                                                count++;
                                            }
                                        }
                                        if (count > 0) {
                                            movie.setRating(totalRating / count);
                                        } else {
                                            movie.setRating(0);
                                        }
                                        favoriteMovies.add(movie);

                                        // Cập nhật adapter sau khi lấy đủ thông tin
                                        if (favoriteMovies.size() == favoriteMovieIds.size()) {
                                            favoritesAdapter.notifyDataSetChanged();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e(TAG, "Error loading ratings for movie " + movie.getId() + ": " + error.getMessage());
                                        movie.setRating(0);
                                        favoriteMovies.add(movie);

                                        if (favoriteMovies.size() == favoriteMovieIds.size()) {
                                            favoritesAdapter.notifyDataSetChanged();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error loading movie " + movieId + ": " + error.getMessage());
                            if (favoriteMovies.size() == favoriteMovieIds.size()) {
                                favoritesAdapter.notifyDataSetChanged();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading favorites: " + error.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void openVideoPlayer(String videoUrl) {
        if (videoUrl != null && !videoUrl.isEmpty()) {
            Intent intent = new Intent(FavoritesActivity.this, VideoPlayerActivity.class);
            intent.putExtra("videoUrl", videoUrl);
            startActivity(intent);
        } else {
            Log.w(TAG, "Video not available");
        }
    }

    private void submitRating(String movieId, float rating) {
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "User not logged in, redirecting to LoginActivity for rating");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference ratingRef = FirebaseDatabase.getInstance().getReference("ratings")
                .child(movieId).child(userId);
        ratingRef.setValue(rating)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Rating submitted for movie " + movieId + ": " + rating);
                    // Cập nhật rating trung bình trong danh sách
                    updateAverageRating(movieId);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error submitting rating for movie " + movieId + ": " + e.getMessage()));
    }

    private void updateAverageRating(String movieId) {
        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("ratings").child(movieId);
        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float totalRating = 0;
                int count = 0;
                for (DataSnapshot userRating : snapshot.getChildren()) {
                    Float rating = userRating.getValue(Float.class);
                    if (rating != null) {
                        totalRating += rating;
                        count++;
                    }
                }
                float averageRating = count > 0 ? totalRating / count : 0;
                // Cập nhật rating trung bình cho movie trong danh sách
                for (Movie movie : favoriteMovies) {
                    if (movie.getId().equals(movieId)) {
                        movie.setRating(averageRating);
                    }
                }
                favoritesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error updating average rating for movie " + movieId + ": " + error.getMessage());
            }
        });
    }

    private void toggleFavorite(String movieId, boolean isFavorite) {
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "User not logged in, redirecting to LoginActivity for favorite");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference favoriteRef = FirebaseDatabase.getInstance().getReference("favorites")
                .child(userId).child(movieId);
        if (isFavorite) {
            favoriteRef.setValue(true)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Added favorite: " + movieId))
                    .addOnFailureListener(e -> Log.e(TAG, "Error adding favorite: " + e.getMessage()));
        } else {
            favoriteRef.removeValue()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Removed favorite: " + movieId))
                    .addOnFailureListener(e -> Log.e(TAG, "Error removing favorite: " + e.getMessage()));
        }
    }
}