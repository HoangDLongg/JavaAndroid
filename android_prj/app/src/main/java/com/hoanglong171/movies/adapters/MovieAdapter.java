package com.hoanglong171.movies.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoanglong171.movies.R;
import com.hoanglong171.movies.models.Movie;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private List<Movie> movies;
    private OnMovieClickListener movieClickListener;
    private OnRatingSubmitListener ratingSubmitListener;
    private OnFavoriteToggleListener favoriteToggleListener;

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    public interface OnRatingSubmitListener {
        void onRatingSubmit(String movieId, float rating);
    }

    public interface OnFavoriteToggleListener {
        void onFavoriteToggle(String movieId, boolean isFavorite); // Cập nhật để nhận 2 tham số
    }

    public MovieAdapter(List<Movie> movies, OnMovieClickListener movieClickListener,
                        OnRatingSubmitListener ratingSubmitListener, OnFavoriteToggleListener favoriteToggleListener) {
        this.movies = movies;
        this.movieClickListener = movieClickListener;
        this.ratingSubmitListener = ratingSubmitListener;
        this.favoriteToggleListener = favoriteToggleListener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.titleTextView.setText(movie.getTitle());
        holder.descriptionTextView.setText(movie.getDescription());
        Glide.with(holder.itemView.getContext())
                .load(movie.getThumbnailUrl())
                .into(holder.thumbnailImageView);

        // Hiển thị rating trung bình từ model Movie
        if (movie.getRating() > 0) {
            holder.ratingTextView.setText(String.format("Avg: %.1f", movie.getRating()));
        } else {
            holder.ratingTextView.setText("No ratings yet");
        }

        // Lấy rating của người dùng hiện tại từ Firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            DatabaseReference userRatingRef = FirebaseDatabase.getInstance().getReference("ratings")
                    .child(movie.getId()).child(userId);
            userRatingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Float userRating = snapshot.getValue(Float.class);
                        if (userRating != null) {
                            holder.ratingBar.setRating(userRating);
                        } else {
                            holder.ratingBar.setRating(0);
                        }
                    } else {
                        holder.ratingBar.setRating(0);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    holder.ratingBar.setRating(0);
                }
            });
        } else {
            holder.ratingBar.setRating(0);
        }

        holder.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                ratingSubmitListener.onRatingSubmit(movie.getId(), rating);
            }
        });

        // Cập nhật icon favorite
        holder.favoriteIcon.setImageResource(movie.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        holder.favoriteIcon.setOnClickListener(v -> {
            boolean newFavoriteState = !movie.isFavorite(); // Đảo ngược trạng thái yêu thích
            favoriteToggleListener.onFavoriteToggle(movie.getId(), newFavoriteState);
            movie.setFavorite(newFavoriteState); // Cập nhật trạng thái trong model
            holder.favoriteIcon.setImageResource(newFavoriteState ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        });

        holder.itemView.setOnClickListener(v -> movieClickListener.onMovieClick(movie));
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView, ratingTextView;
        ImageView thumbnailImageView;
        RatingBar ratingBar;
        ImageView favoriteIcon;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_view_title);
            descriptionTextView = itemView.findViewById(R.id.text_view_description);
            ratingTextView = itemView.findViewById(R.id.text_view_rating);
            thumbnailImageView = itemView.findViewById(R.id.image_view_thumbnail);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            favoriteIcon = itemView.findViewById(R.id.favorite_icon);
        }
    }
}