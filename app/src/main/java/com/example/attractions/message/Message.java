package com.example.attractions.message;

public class Message {

    private String id, ownerId, text, photoUrl, date;

    public Message(String id, String ownerId, String text, String photoUrl, String date) {
        this.id = id;
        this.ownerId = ownerId;
        this.text = text;
        this.photoUrl = photoUrl;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
