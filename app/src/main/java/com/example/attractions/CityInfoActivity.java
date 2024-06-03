package com.example.attractions;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.attractions.databinding.ActivityCityInfoBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class CityInfoActivity extends AppCompatActivity {

    private ActivityCityInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCityInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String cityId = getIntent().getStringExtra("city");

        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
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

                        binding.linearLayout.setVisibility(View.VISIBLE);
                        binding.progressBar.setVisibility(View.INVISIBLE);
                    } catch (Exception ignored) {
                        showToast("Data upload error. Please try again later...");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // do nothing
            }

            private void showToast(String text) {
                try {
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                } catch (NullPointerException e) {
                    // do nothing
                }
            }
        });

    }
}