package com.example.attractions.cities;

import android.os.Parcel;
import android.os.Parcelable;

public class City implements Parcelable {
    public final String id;
    public final String title;
    public final String description;
    public final String imgPoster;
    public final String releaseDate;

    public City() {
        this.id = "";
        this.title = "";
        this.description = "";
        this.imgPoster = "";
        this.releaseDate = "";
    }

    private City(Parcel parcel) {
        this.id = parcel.readString();
        this.title = parcel.readString();
        this.description = parcel.readString();
        this.imgPoster = parcel.readString();
        this.releaseDate = parcel.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(imgPoster);
        parcel.writeString(releaseDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<City> CREATOR = new Parcelable.Creator<City>() {
        @Override
        public City createFromParcel(Parcel parcel) {
            return new City(parcel);
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };
}
