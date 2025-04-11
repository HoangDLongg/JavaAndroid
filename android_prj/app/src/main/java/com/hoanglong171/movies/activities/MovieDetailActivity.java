package com.hoanglong171.movies.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoanglong171.movies.R;
import com.hoanglong171.movies.models.Movie;

import androidx.annotation.NonNull;

public class MovieDetailActivity extends AppCompatActivity {
    private ImageView thumbnailImageView;
    private TextView titleTextView, descriptionTextView, ratingTextView;
    private RatingBar ratingBar;
    private Button favoriteButton;
    private FirebaseAuth auth;
    private DatabaseReference favoritesRef;
    private DatabaseReference ratingsRef;
    private String movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        // Thêm Toolbar để hỗ trợ nút quay lại
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Movie Details");
        }

        // Ánh xạ các thành phần
        thumbnailImageView = findViewById(R.id.detail_image_view_thumbnail);
        titleTextView = findViewById(R.id.detail_text_view_title);
        descriptionTextView = findViewById(R.id.detail_text_view_description);
        ratingTextView = findViewById(R.id.detail_text_view_rating);
        ratingBar = findViewById(R.id.detail_rating_bar);
        favoriteButton = findViewById(R.id.detail_button_favorite);

        // Lấy dữ liệu từ Intent
        movieId = getIntent().getStringExtra("movieId");
        if (movieId == null) {
            Toast.makeText(this, "Movie ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "guest";
        favoritesRef = FirebaseDatabase.getInstance().getReference("favorites").child(userId);
        ratingsRef = FirebaseDatabase.getInstance().getReference("ratings").child(movieId);

        // Lấy thông tin phim từ Realtime Database
        DatabaseReference movieRef = FirebaseDatabase.getInstance().getReference("movies").child(movieId);
        movieRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Movie movie = snapshot.getValue(Movie.class);
                if (movie != null) {
                    movie.setId(movieId);
                    displayMovieDetails(movie);
                    checkFavoriteStatus();
                    loadAverageRating();
                } else {
                    Toast.makeText(MovieDetailActivity.this, "Movie not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MovieDetailActivity.this, "Error loading movie: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Xử lý rating
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                submitRating(rating);
            }
        });

        // Xử lý nút yêu thích
        favoriteButton.setOnClickListener(v -> toggleFavorite());
    }

    private void displayMovieDetails(Movie movie) {
        titleTextView.setText(movie.getTitle());
        descriptionTextView.setText(movie.getDescription());
        Glide.with(this).load(movie.getThumbnailUrl()).into(thumbnailImageView);
    }

    private void loadAverageRating() {
        ratingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    ratingTextView.setText("No ratings yet");
                    return;
                }

                float totalRating = 0;
                int count = 0;
                for (DataSnapshot ratingSnapshot : snapshot.getChildren()) {
                    Float rating = ratingSnapshot.getValue(Float.class);
                    if (rating != null) {
                        totalRating += rating;
                        count++;
                    }
                }

                if (count > 0) {
                    float averageRating = totalRating / count;
                    ratingTextView.setText(String.format("Average Rating: %.1f (%d votes)", averageRating, count));
                } else {
                    ratingTextView.setText("No ratings yet");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MovieDetailActivity.this, "Error loading ratings: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkFavoriteStatus() {
        favoritesRef.child(movieId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isFavorite = snapshot.exists() && snapshot.getValue(Boolean.class) == Boolean.TRUE;
                favoriteButton.setText(isFavorite ? "Remove from Favorites" : "Add to Favorites");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MovieDetailActivity.this, "Error checking favorite: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitRating(float rating) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "guest";
        DatabaseReference ratingRef = ratingsRef.child(userId);
        ratingRef.setValue(rating)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Rating submitted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error submitting rating", Toast.LENGTH_SHORT).show());
    }

    private void toggleFavorite() {
        favoritesRef.child(movieId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    favoritesRef.child(movieId).removeValue();
                } else {
                    favoritesRef.child(movieId).setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MovieDetailActivity.this, "Error toggling favorite: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {

        return true;
    }
}