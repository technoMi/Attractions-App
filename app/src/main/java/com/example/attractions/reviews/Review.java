package com.example.attractions.reviews;

public class Review {

    public String date, photoUrl, profileImageUrl, text, username;

    public Review(String date, String photoUrl, String profileImageUrl, String text, String username) {
        this.date = date;
        this.photoUrl = photoUrl;
        this.profileImageUrl = profileImageUrl;
        this.text = text;
        this.username = username;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

}
