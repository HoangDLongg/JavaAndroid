package com.hoanglong171.movies.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private RecyclerView recyclerViewTopMovies, recyclerViewUpMovies;
    private MovieAdapter topMoviesAdapter, upMoviesAdapter;
    private List<Movie> topMovies = new ArrayList<>();
    private List<Movie> upMovies = new ArrayList<>();
    private List<Movie> originalTopMovies = new ArrayList<>();
    private List<Movie> originalUpMovies = new ArrayList<>();
    private FirebaseAuth auth;
    private DatabaseReference moviesRef;
    private DatabaseReference favoritesRef;
    private DatabaseReference ratingsRef;
    private DatabaseReference usersRef;
    private DatabaseReference userRef;
    private ProgressBar progressBar2, progressBar3;
    private ChipNavigationBar chipNavigationBar;
    private ImageView userAvatar;
    private TextView userName, userEmail;
    private EditText searchEditText;
    private Map<String, Boolean> favoritesMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ các thành phần
        recyclerViewTopMovies = findViewById(R.id.recyclerViewTopMovies);
        recyclerViewUpMovies = findViewById(R.id.recyclerViewUpMovies);
        progressBar2 = findViewById(R.id.progressBar2);
        progressBar3 = findViewById(R.id.progressBar3);
        chipNavigationBar = findViewById(R.id.chipNavigationBar);
        userAvatar = findViewById(R.id.imageView3);
        userName = findViewById(R.id.textView4);
        userEmail = findViewById(R.id.textView6);
        searchEditText = findViewById(R.id.editTextText);

        // Cấu hình LayoutManager cho RecyclerView (ngang)
        recyclerViewTopMovies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewTopMovies.setNestedScrollingEnabled(false);
        recyclerViewUpMovies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewUpMovies.setNestedScrollingEnabled(false);

        // Kiểm tra kết nối Firebase
        checkFirebaseConnection();

        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "User not logged in, redirecting to LoginActivity");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        String userId = auth.getCurrentUser().getUid();

        // Khởi tạo Adapter với các sự kiện click
        topMoviesAdapter = new MovieAdapter(topMovies, movie -> {
            openVideoPlayer(movie.getVideoUrl());
        }, (movieId, rating) -> submitRating(movieId, rating), (movieId, isFavorite) -> toggleFavorite(movieId, isFavorite));
        upMoviesAdapter = new MovieAdapter(upMovies, movie -> {
            openVideoPlayer(movie.getVideoUrl());
        }, (movieId, rating) -> submitRating(movieId, rating), (movieId, isFavorite) -> toggleFavorite(movieId, isFavorite));

        recyclerViewTopMovies.setAdapter(topMoviesAdapter);
        recyclerViewUpMovies.setAdapter(upMoviesAdapter);

        // Khởi tạo tham chiếu Firebase
        moviesRef = FirebaseDatabase.getInstance().getReference("movies");
        favoritesRef = FirebaseDatabase.getInstance().getReference("favorites").child(userId);
        ratingsRef = FirebaseDatabase.getInstance().getReference("ratings");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        userRef = usersRef.child(userId);

        // Tải thông tin người dùng
        loadUserInfo();

        // Tự động thêm UID của tất cả người dùng khi có phim mới (chỉ thêm favorites, không thêm ratings)
        autoAddUsersToNewMovie();

        // Tải danh sách phim
        setupRealtimeListeners(userId);

        // Ánh xạ và xử lý nút đăng xuất
        Button logoutBtn = findViewById(R.id.btnLogout);
        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, IntroActivity.class);
            startActivity(intent);
            finish();
        });

        // Xử lý ChipNavigationBar
        setupChipNavigationBar();

        // Xử lý tìm kiếm phim
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchEditText.getText().toString().trim().toLowerCase();
            filterMovies(query);
            return true;
        });
    }

    private void loadUserInfo() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String avatarBase64 = snapshot.child("avatarBase64").getValue(String.class);

                    // Hiển thị thông tin
                    userName.setText("Hello " + (name != null ? name : "User"));
                    userEmail.setText(email != null ? email : "N/A");

                    // Hiển thị avatar
                    if (avatarBase64 != null) {
                        try {
                            byte[] decodedString = Base64.decode(avatarBase64, Base64.DEFAULT);
                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            userAvatar.setImageBitmap(decodedBitmap);
                        } catch (Exception e) {
                            Log.e(TAG, "Error decoding avatar: " + e.getMessage());
                            userAvatar.setImageResource(R.drawable.profile);
                        }
                    }
                } else {
                    Log.w(TAG, "Không tìm thấy thông tin người dùng");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi tải thông tin người dùng: " + error.getMessage());
            }
        });
    }

    private void autoAddUsersToNewMovie() {
        moviesRef.addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                String movieId = snapshot.getKey();
                Log.d(TAG, "New movie added: " + movieId);

                // Lấy danh sách tất cả người dùng từ node users
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                        for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                            String userId = userSnapshot.getKey();

                            // Thêm UID vào favorites với giá trị mặc định false
                            DatabaseReference movieFavoriteRef = FirebaseDatabase.getInstance().getReference("favorites").child(userId).child(movieId);
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
                        Log.e(TAG, "Error loading users: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Không cần xử lý khi phim thay đổi
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Không cần xử lý khi phim bị xóa
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Không cần xử lý khi phim thay đổi vị trí
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error listening to movies: " + error.getMessage());
            }
        });
    }

    private void setupRealtimeListeners(String userId) {
        progressBar2.setVisibility(View.VISIBLE);
        progressBar3.setVisibility(View.VISIBLE);

        // Bước 1: Tải danh sách favorites trước
        favoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoritesMap.clear();
                for (DataSnapshot favoriteSnapshot : snapshot.getChildren()) {
                    String movieId = favoriteSnapshot.getKey();
                    Boolean isFavorite = favoriteSnapshot.getValue(Boolean.class);
                    if (isFavorite != null && isFavorite) {
                        favoritesMap.put(movieId, true);
                    }
                }
                Log.d(TAG, "Favorites loaded: " + favoritesMap.toString());

                // Bước 2: Tải danh sách phim sau khi đã có favorites
                loadMovies();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading favorites: " + error.getMessage());
                // Tiếp tục tải phim ngay cả khi không lấy được favorites
                loadMovies();
            }
        });

        // Lắng nghe thay đổi favorites để cập nhật giao diện
        favoritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoritesMap.clear();
                for (DataSnapshot favoriteSnapshot : snapshot.getChildren()) {
                    String movieId = favoriteSnapshot.getKey();
                    Boolean isFavorite = favoriteSnapshot.getValue(Boolean.class);
                    if (isFavorite != null && isFavorite) {
                        favoritesMap.put(movieId, true);
                    }
                }
                // Cập nhật trạng thái favorite cho các phim
                for (Movie movie : topMovies) {
                    movie.setFavorite(favoritesMap.containsKey(movie.getId()));
                }
                for (Movie movie : upMovies) {
                    movie.setFavorite(favoritesMap.containsKey(movie.getId()));
                }
                topMoviesAdapter.notifyDataSetChanged();
                upMoviesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error listening to favorites: " + error.getMessage());
            }
        });
    }

    private void loadMovies() {
        moviesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Data snapshot received: " + snapshot.toString());
                List<Movie> allMovies = new ArrayList<>();
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    Movie movie = movieSnapshot.getValue(Movie.class);
                    if (movie != null) {
                        movie.setId(movieSnapshot.getKey());
                        movie.setFavorite(favoritesMap.containsKey(movie.getId()));
                        Log.d(TAG, "Loaded movie: " + (movie.getTitle() != null ? movie.getTitle() : "Unknown") +
                                ", Video URL: " + (movie.getVideoUrl() != null ? movie.getVideoUrl() : "null") +
                                ", Thumbnail URL: " + (movie.getThumbnailUrl() != null ? movie.getThumbnailUrl() : "null"));

                        // Lấy rating trung bình từ Firebase
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
                                    movie.setRating(0); // Giữ 0 nếu không có rating, nhưng không tự động thêm
                                }
                                allMovies.add(movie);

                                // Cập nhật danh sách khi tất cả phim đã được xử lý
                                if (allMovies.size() == snapshot.getChildrenCount()) {
                                    updateMovieLists(allMovies);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Error loading ratings for movie " + movie.getId() + ": " + error.getMessage());
                                movie.setRating(0);
                                allMovies.add(movie);

                                if (allMovies.size() == snapshot.getChildrenCount()) {
                                    updateMovieLists(allMovies);
                                }
                            }
                        });
                    } else {
                        Log.w(TAG, "Failed to parse movie: " + movieSnapshot.getKey() + ", Raw data: " + movieSnapshot.getValue());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading movies: " + error.getMessage());
                progressBar2.setVisibility(View.GONE);
                progressBar3.setVisibility(View.GONE);
            }
        });
    }

    private void updateMovieLists(List<Movie> allMovies) {
        if (allMovies.isEmpty()) {
            Log.w(TAG, "No movies available in Firebase");
            progressBar2.setVisibility(View.GONE);
            progressBar3.setVisibility(View.GONE);
            topMoviesAdapter.notifyDataSetChanged();
            upMoviesAdapter.notifyDataSetChanged();
            return;
        }

        // Lưu danh sách gốc
        originalTopMovies.clear();
        originalUpMovies.clear();
        for (Movie movie : allMovies) {
            if (movie.getRating() > 4.0) {
                originalTopMovies.add(movie);
            }
        }
        originalUpMovies.addAll(allMovies);

        // Xóa danh sách hiện tại
        topMovies.clear();
        upMovies.clear();

        // Lọc phim có rating > 4.0 cho topMovies
        for (Movie movie : allMovies) {
            if (movie.getRating() > 4.0) { // Chỉ lấy phim có rating lớn hơn 4.0
                topMovies.add(movie);
            }
        }

        // Sắp xếp topMovies theo rating giảm dần (tùy chọn, để hiển thị phim tốt nhất lên đầu)
        topMovies.sort((movie1, movie2) -> Float.compare(movie2.getRating(), movie1.getRating()));

        // Thêm tất cả phim vào upMovies (bất kể rating)
        upMovies.addAll(allMovies);

        // Cập nhật Adapter
        topMoviesAdapter.notifyDataSetChanged();
        upMoviesAdapter.notifyDataSetChanged();
        progressBar2.setVisibility(View.GONE);
        progressBar3.setVisibility(View.GONE);
    }

    private void filterMovies(String query) {
        List<Movie> filteredTopMovies = new ArrayList<>();
        List<Movie> filteredUpMovies = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            // Nếu không có từ khóa tìm kiếm, khôi phục danh sách gốc
            topMovies.clear();
            upMovies.clear();
            for (Movie movie : originalTopMovies) {
                topMovies.add(movie);
            }
            upMovies.addAll(originalUpMovies);
        } else {
            // Lọc danh sách theo từ khóa
            query = query.toLowerCase();

            for (Movie movie : originalTopMovies) {
                if (movie.getTitle() != null && movie.getTitle().toLowerCase().contains(query)) {
                    filteredTopMovies.add(movie);
                }
            }

            for (Movie movie : originalUpMovies) {
                if (movie.getTitle() != null && movie.getTitle().toLowerCase().contains(query)) {
                    filteredUpMovies.add(movie);
                }
            }

            // Lọc lại topMovies để chỉ giữ phim có rating > 4.0
            topMovies.clear();
            for (Movie movie : filteredTopMovies) {
                if (movie.getRating() > 4.0) {
                    topMovies.add(movie);
                }
            }

            // Cập nhật upMovies với tất cả phim khớp từ khóa
            upMovies.clear();
            upMovies.addAll(filteredUpMovies);
        }

        // Sắp xếp topMovies theo rating giảm dần
        topMovies.sort((movie1, movie2) -> Float.compare(movie2.getRating(), movie1.getRating()));

        // Cập nhật Adapter
        topMoviesAdapter.notifyDataSetChanged();
        upMoviesAdapter.notifyDataSetChanged();
    }

    private void openVideoPlayer(String videoUrl) {
        if (videoUrl != null && !videoUrl.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
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
                    // Cập nhật rating trung bình và kiểm tra topMovies
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
                Log.d(TAG, "Updated average rating for movie " + movieId + ": " + averageRating);

                // Cập nhật rating trung bình cho tất cả movie trong danh sách
                boolean movieUpdated = false;
                for (Movie movie : upMovies) {
                    if (movie.getId().equals(movieId)) {
                        movie.setRating(averageRating);
                        movieUpdated = true;
                        break;
                    }
                }
                if (!movieUpdated) {
                    // Nếu movie không tồn tại trong upMovies, thêm nó (nếu cần)
                    for (Movie movie : originalUpMovies) {
                        if (movie.getId().equals(movieId)) {
                            movie.setRating(averageRating);
                            upMovies.add(movie);
                            break;
                        }
                    }
                }

                // Cập nhật topMovies dựa trên rating trung bình
                topMovies.clear();
                for (Movie movie : originalUpMovies) {
                    if (movie.getId().equals(movieId)) {
                        movie.setRating(averageRating);
                    }
                    if (movie.getRating() > 4.0) {
                        topMovies.add(movie);
                    }
                }
                topMovies.sort((movie1, movie2) -> Float.compare(movie2.getRating(), movie1.getRating()));

                // Cập nhật Adapter
                topMoviesAdapter.notifyDataSetChanged();
                upMoviesAdapter.notifyDataSetChanged();
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

    private void setupChipNavigationBar() {
        chipNavigationBar.setOnItemSelectedListener(id -> {
            if (id == R.id.nav_home) {
                // Đã ở MainActivity, không làm gì
            } else if (id == R.id.nav_favorites) {
                try {
                    Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening FavoritesActivity: " + e.getMessage());
                }
            } else if (id == R.id.nav_profile) {
                try {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening ProfileActivity: " + e.getMessage());
                }
            }
        });

        // Đặt mục "Home" được chọn mặc định
        chipNavigationBar.setItemSelected(R.id.nav_home, true);
    }

    private void checkFirebaseConnection() {
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                Log.d("FirebaseConnection", "Trạng thái: " + connected);
                if (!connected) {
                    Log.w("FirebaseConnection", "Không có kết nối đến Firebase!");
                } else {
                    Log.d("FirebaseConnection", "Kết nối Firebase thành công!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseConnection", "Lỗi: " + error.getMessage());
            }
        });
    }
}