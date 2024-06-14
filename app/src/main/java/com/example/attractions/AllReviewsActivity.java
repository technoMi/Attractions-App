package com.example.attractions;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attractions.databinding.AllReviewsActivityBinding;
import com.example.attractions.message.Message;
import com.example.attractions.message.MessagesAdapter;
import com.example.attractions.reviews.ReviewsAdapter;
import com.example.attractions.reviews.Review;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AllReviewsActivity extends AppCompatActivity {

    private AllReviewsActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AllReviewsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String attractionId = getIntent().getStringExtra("attractionId");

        loadReviews(attractionId);

        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void loadReviews(String attractionId){
        if (attractionId==null) return;

        FirebaseDatabase.getInstance().getReference().child("Reviews")
                .child(attractionId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        List<Review> reviews = new ArrayList<>();
                        for (DataSnapshot review : snapshot.getChildren()){

                            String date = review.child("date").getValue() != null ? review.child("date").getValue().toString() : "";
                            String text = review.child("text").getValue() != null ? review.child("text").getValue().toString() : "";
                            String username = review.child("username").getValue() != null ? review.child("username").getValue().toString() : "";
                            String photoUrl = review.child("photoUrl").getValue() != null ? review.child("photoUrl").getValue().toString() : "";
                            String profileImageUrl = review.child("profileImageUrl").getValue() != null ? review.child("profileImageUrl").getValue().toString() : "";

                            reviews.add(new Review(date, photoUrl, profileImageUrl, text, username));
                        }

                        LinearLayoutManager llm = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false);
//                        llm.setStackFromEnd(true);
                        binding.messagesRv.setLayoutManager(llm);
                        binding.messagesRv.setAdapter(new ReviewsAdapter(reviews));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // do nothing
                    }
                });
    }
}
