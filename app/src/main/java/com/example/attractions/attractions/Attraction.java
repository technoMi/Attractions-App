package com.example.attractions.attractions;

public class Attraction {
    private final String attraction_id;
    private final String attraction_name;
    private final String attraction_rating;
    private final String attraction_imgPoster;

    public Attraction(String attraction_id, String attraction_name, String attraction_rating, String attraction_imgPoster) {
        this.attraction_id = attraction_id;
        this.attraction_name = attraction_name;
        this.attraction_rating = attraction_rating;
        this.attraction_imgPoster = attraction_imgPoster;
    }

    public String getAttraction_id() {
        return attraction_id;
    }

    public String attraction_imgPoster() {
        return attraction_imgPoster;
    }

    public String getAttraction_name() {
        return attraction_name;
    }

    public String getAttraction_rating() {
        return attraction_rating;
    }
}