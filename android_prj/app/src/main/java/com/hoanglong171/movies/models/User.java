package com.hoanglong171.movies.models;

public class User {
    private String uid;
    private String name;
    private String email;
    private String dateOfBirth;
    private String city;
    private String avatarBase64;
    private String role; // "user" hoặc "admin"

    public User() {}



    // Getters và Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAvatarBase64() { return avatarBase64; }
    public void setAvatarBase64(String avatarBase64) { this.avatarBase64 = avatarBase64; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public User(String uid, String name, String email, String dateOfBirth, String city, String avatarBase64, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.city = city;
        this.avatarBase64 = avatarBase64;
        this.role = role;
        this.avatarBase64 = null;
    }
}