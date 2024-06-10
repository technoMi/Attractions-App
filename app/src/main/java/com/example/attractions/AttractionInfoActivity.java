package com.example.attractions;

import static java.util.Objects.requireNonNull;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.attractions.databinding.ActivityAttractionInfoBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class AttractionInfoActivity extends AppCompatActivity {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    private final String currentUserId = requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    private String attractionId;

    private Boolean isUserEvaluationExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAttractionInfoBinding binding = ActivityAttractionInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        attractionId = getIntent().getStringExtra("attractionId");

        userEvaluationCheck(binding);

        assert attractionId != null;
        database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                DataSnapshot attractionSnapshot = snapshot.child("Attractions").child(attractionId);

                if (!snapshot.exists()) {
                    Toast.makeText(getApplicationContext(), "\"Data upload error. Please try again later...", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String title = requireNonNull(attractionSnapshot.child("title").getValue()).toString();
                        String description = requireNonNull(attractionSnapshot.child("description").getValue()).toString();
                        String imgUrl = requireNonNull(attractionSnapshot.child("imgBg").getValue()).toString();
                        String workingHours = requireNonNull(attractionSnapshot.child("workingHours").getValue()).toString();
                        String rating = String.format(requireNonNull(attractionSnapshot.child("rating").getValue()).toString());
                        String formattedRating = String.format("%.1f", Float.parseFloat(rating));

                        binding.attractionNameTitle.setText(title);
                        binding.attractionDescription.setText(description);
                        binding.workingHours.setText(workingHours);

                        binding.attractionRating.setText("⭐ " + formattedRating);

                        Glide.with(getApplicationContext()).load(imgUrl).into(binding.imgAttractionLandscape);

                        binding.progressBar.setVisibility(View.INVISIBLE);
                        binding.scrollView.setVisibility(View.VISIBLE);
                    } catch (Exception ignored) {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //do nothing
            }
        });

        binding.ratingBar.setOnRatingBarChangeListener(
                (ratingBar1, rating, fromUser) ->
                        database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try {
                                    if (!isUserEvaluationExist) {
                                        DatabaseReference ratingReference = snapshot
                                                .child("Attractions")
                                                .child(attractionId)
                                                .child("rating")
                                                .getRef();
                                        DatabaseReference numberOfEvaluationReference = snapshot
                                                .child("Attractions")
                                                .child(attractionId)
                                                .child("numberOfEvaluation")
                                                .getRef();
                                        Float currentRating = snapshot.child("Attractions")
                                                .child(attractionId)
                                                .child("rating")
                                                .getValue(Float.class);
                                        Float numberOfEvaluation = snapshot.child("Attractions")
                                                .child(attractionId)
                                                .child("numberOfEvaluation")
                                                .getValue(Float.class);

                                        float userRating = Float.parseFloat(String.valueOf(rating));

                                        if (currentRating != null && numberOfEvaluation != null) {
                                            Float newRating = ((currentRating * numberOfEvaluation + userRating) / (numberOfEvaluation + 1));

                                            String formattedRating = String.format(Locale.US, "%.1f", newRating);

                                            ratingReference.setValue(Float.parseFloat(formattedRating));
                                            numberOfEvaluationReference.setValue(numberOfEvaluation + 1);

                                            DatabaseReference currentUserReference = snapshot
                                                    .child("Users")
                                                    .child(currentUserId)
                                                    .child("evaluations")
                                                    .child(attractionId)
                                                    .getRef();

                                            currentUserReference.setValue(Float.parseFloat(formattedRating));

                                            changeBindingState(binding, userRating);
                                        } else {
                                            throw new NullPointerException();
                                        }
                                    } else {
                                        // todo написать тост
                                    }
                                } catch (Exception ignored) {
                                    // todo написать тост
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                //do nothing
                            }
                        }
                        ));

    }

    private void userEvaluationCheck(ActivityAttractionInfoBinding binding) {
        database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot userEvaluationsSnapshot = snapshot
                        .child("Users")
                        .child(currentUserId)
                        .child("evaluations")
                        .child(attractionId);
                if (userEvaluationsSnapshot.exists()) {
                    isUserEvaluationExist = true;
                    Float userEvaluation = userEvaluationsSnapshot
                            .getValue(Float.class);
                    changeBindingState(binding, userEvaluation);
                } else {
                    isUserEvaluationExist = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //do nothing
            }
        });
    }

    private void changeBindingState(ActivityAttractionInfoBinding binding, Float userEvaluation) {
        binding.ratingHint.setText("Ваша оценка:");
        binding.ratingBar.setRating(userEvaluation);
        binding.ratingBar.setIsIndicator(true);
    }
}