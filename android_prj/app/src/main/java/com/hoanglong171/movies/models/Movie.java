package com.hoanglong171.movies.models;

public class Movie {
    private String id;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private boolean isFavorite;
    private float rating;

    public Movie() {}

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
}