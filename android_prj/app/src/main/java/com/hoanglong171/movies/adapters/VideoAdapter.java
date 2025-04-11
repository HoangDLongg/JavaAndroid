package com.hoanglong171.movies.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hoanglong171.movies.R;
import com.hoanglong171.movies.models.Movie;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<Movie> videoList;
    private OnVideoActionListener actionListener;

    public interface OnVideoActionListener {
        void onEdit(Movie movie);
        void onDelete(Movie movie);
    }

    public VideoAdapter(List<Movie> videoList, OnVideoActionListener actionListener) {
        this.videoList = videoList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Movie movie = videoList.get(position);
        holder.tvVideoTitle.setText(movie.getTitle());
        holder.tvVideoDescription.setText(movie.getDescription());

        holder.btnEditVideo.setOnClickListener(v -> actionListener.onEdit(movie));
        holder.btnDeleteVideo.setOnClickListener(v -> actionListener.onDelete(movie));
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView tvVideoTitle, tvVideoDescription;
        Button btnEditVideo, btnDeleteVideo;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVideoTitle = itemView.findViewById(R.id.tvVideoTitle);
            tvVideoDescription = itemView.findViewById(R.id.tvVideoDescription);
            btnEditVideo = itemView.findViewById(R.id.btnEditVideo);
            btnDeleteVideo = itemView.findViewById(R.id.btnDeleteVideo);
        }
    }
}