package com.example.attractions;

import static java.util.Objects.requireNonNull;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.attractions.databinding.ActivityAttractionInfoBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.EventListener;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AttractionInfoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityAttractionInfoBinding binding;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    private final String currentUserId = requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    private String attractionId;

    private Boolean isUserEvaluationExist;

    private String[] coordinates = null;

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot dataSnapshot = snapshot.child("Attractions")
                        .child(attractionId)
                        .child("coordinates");

                googleMap.getUiSettings().setZoomGesturesEnabled(false);
                googleMap.getUiSettings().setScrollGesturesEnabled(false);

                if (dataSnapshot.exists()) {
                    String crd = dataSnapshot.getValue(String.class);

                    assert crd != null;
                    coordinates = crd.split("/");

                    LatLng location = new LatLng(Float.parseFloat(coordinates[0]), Float.parseFloat(coordinates[1]));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14));
                    MarkerOptions options = new MarkerOptions().position(location).title(getString(R.string.location));
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    googleMap.addMarker(options);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.map_data_error), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // do nothing
            }
        });

        googleMap.setOnMapClickListener(latLng -> {
            if (coordinates != null) {
                Intent mapApp = new Intent();
                mapApp.setAction(Intent.ACTION_VIEW);
                mapApp.setData(Uri.parse("geo:" + coordinates[0] + ", " + coordinates[1]));
                startActivity(mapApp);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAttractionInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        attractionId = getIntent().getStringExtra("attractionId");

        userEvaluationCheck(binding);

        favoriteAttractionCheck(binding);

        checkForFeedback();

        loadSingleReview();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        assert attractionId != null;
        database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                DataSnapshot attractionSnapshot = snapshot.child("Attractions").child(attractionId);

                if (!snapshot.exists()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.data_upload_error), Toast.LENGTH_SHORT).show();
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
                        // do nothing
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

                                            changeRatingBarState(binding, userRating);
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
                        }));

        binding.favoriteButton.setOnClickListener(v ->
                database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DataSnapshot dataSnapshot = snapshot
                                .child("Users")
                                .child(currentUserId)
                                .child("favorites")
                                .child(attractionId);

                        DatabaseReference reference = dataSnapshot.getRef();
                        if (dataSnapshot.exists()) {
                            reference.removeValue();
                            Toast.makeText(getApplicationContext(), getString(R.string.deleted_from_favorites), Toast.LENGTH_SHORT).show();
                        } else {
                            reference.setValue(attractionId);
                            Toast.makeText(getApplicationContext(), getString(R.string.add_to_favorites), Toast.LENGTH_SHORT).show();
                        }
                        favoriteAttractionCheck(binding);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //do nothing
                    }
                }));

        binding.leaveReview.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewReviewActivity.class);
            intent.putExtra("attractionId", attractionId);
            startActivity(intent);
        });
        binding.backBtn.setOnClickListener(v -> finish());
        binding.moreReviews.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllReviewsActivity.class);
            intent.putExtra("attractionId", attractionId);
            startActivity(intent);
        });
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
                    changeRatingBarState(binding, userEvaluation);
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

    private void changeRatingBarState(ActivityAttractionInfoBinding binding, Float userEvaluation) {
        binding.ratingHint.setText(getString(R.string.your_evaluation));
        binding.ratingBar.setRating(userEvaluation);
        binding.ratingBar.setIsIndicator(true);
    }

    private void favoriteAttractionCheck(ActivityAttractionInfoBinding binding) {
        database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot dataSnapshot = snapshot
                        .child("Users")
                        .child(currentUserId)
                        .child("favorites")
                        .child(attractionId);

                changeFavoriteButtonState(dataSnapshot.exists(), binding);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //do nothing
            }
        });
    }

    private void changeFavoriteButtonState(Boolean isFavorite, ActivityAttractionInfoBinding binding) {
        if (isFavorite) {
            binding.favoriteButton.setBackgroundResource(R.drawable.ic_favorite);
        } else {
            binding.favoriteButton.setBackgroundResource(R.drawable.ic_favorite_border);
        }
    }

    private void checkForFeedback() {
        String currentUserEmail = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
        assert currentUserEmail != null;
        database.getReference().child("Reviews").child(attractionId).child(String.valueOf(currentUserEmail.hashCode())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.leaveReview.setText("Переписать отзыв");
                    binding.ratingTitleHint.setText("Вы можете переписать Ваш отзыв:");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // do nothing
            }
        });
    }

    private void loadSingleReview() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Reviews").child(attractionId);
        ref.orderByKey().limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot node : dataSnapshot.getChildren()) {
                        String date = node.child("date").getValue(String.class);
                        String text = node.child("text").getValue(String.class);
                        String userName = node.child("username").getValue(String.class);
                        String photoUrl = node.child("photoUrl").getValue(String.class);
                        String profileImageUrl = node.child("profileImageUrl").getValue(String.class);

                        binding.singleReview.reviewText.setText(text);
                        binding.singleReview.reviewDate.setText(date);

                        if (photoUrl != null) {
                            binding.singleReview.reviewPhoto.setVisibility(View.VISIBLE);
                            Glide.with(getApplicationContext()).load(photoUrl).into(binding.singleReview.reviewPhoto);
                        }
                        if (userName != null) {
                            binding.singleReview.reviewUsername.setText(userName);
                            Glide.with(getApplicationContext()).load(profileImageUrl).into(binding.singleReview.reviewProfileIv);
                        }
                    }
                } else {
                    binding.reviewsArea.setVisibility(View.GONE);
                    binding.rivewsTitle.setText("На данный момент отзывов нет");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // do nothing
            }
        });
    }
}
