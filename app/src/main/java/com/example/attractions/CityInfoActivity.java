package com.example.attractions;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.attractions.attractions.Attraction;
//import com.example.attractions.attractions.AttractionAdapter;
import com.example.attractions.attractions.AttractionAdapter;
import com.example.attractions.chats.Chat;
import com.example.attractions.chats.ChatsAdapter;
import com.example.attractions.databinding.ActivityCityInfoBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CityInfoActivity extends AppCompatActivity {

    private ActivityCityInfoBinding binding;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCityInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String cityId = getIntent().getStringExtra("city");

        database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        String cityName = Objects.requireNonNull(snapshot.child("Cities").child(cityId).child("title").getValue()).toString();
                        String cityDescription = Objects.requireNonNull(snapshot.child("Cities").child(cityId).child("description").getValue()).toString();
                        String cityBgImage = Objects.requireNonNull(snapshot.child("Cities").child(cityId).child("imgBg").getValue()).toString();

                        binding.cityNameTitle.setText(cityName);
                        binding.CityDescription.setText(cityDescription);
                        Glide.with(getApplicationContext()).load(cityBgImage).into(binding.imgCityLandscape);

                        String attractionsStr = Objects.requireNonNull((snapshot.child("Cities").child(cityId).child("attractions")).getValue()).toString();
                        String[] attractionsIds = attractionsStr.split(",");

                        ArrayList<Attraction> attractions = new ArrayList<>();

                        for (String attractionId : attractionsIds) {
                            DataSnapshot attractionSnapshot = snapshot.child("Attractions").child(attractionId);
                            String attraction_name = Objects.requireNonNull(attractionSnapshot.child("title").getValue()).toString();
                            String attraction_rating = Objects.requireNonNull(attractionSnapshot.child("rating").getValue()).toString();
                            String attraction_imgPoster = Objects.requireNonNull(attractionSnapshot.child("imgPoster").getValue()).toString();
                            attractions.add(new Attraction(attractionId, attraction_name, attraction_rating, attraction_imgPoster));
                        }

                        binding.attractionsRv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        binding.attractionsRv.setAdapter(new AttractionAdapter(attractions));

                        binding.scrollView.smoothScrollTo(0, 0);

                        binding.linearLayout.setVisibility(View.VISIBLE);
                        binding.progressBar.setVisibility(View.INVISIBLE);
                    } catch (Exception ignored) {
                        showToast(getString(R.string.data_upload_error));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // do nothing
            }

        });
    }

    private void showToast(String text) {
        try {
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            // do nothing
        }
    }
}