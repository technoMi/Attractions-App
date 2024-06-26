package com.example.attractions.bottomnav.profile;

import static java.util.Objects.requireNonNull;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.attractions.R;
import com.example.attractions.attractions.Attraction;
import com.example.attractions.attractions.AttractionAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.attractions.LoginActivity;
import com.example.attractions.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    private final String currentUserId = requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private Uri filePath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        loadUserInfo();

        loadFavorites();

        binding.profileImageView.setOnClickListener(v -> selectImage());

        binding.logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getContext(), LoginActivity.class));
        });

        return binding.getRoot();
    }

    ActivityResultLauncher<Intent> pickImageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        filePath = result.getData().getData();

                        try {
                            Bitmap bitmap = MediaStore.Images.Media
                                    .getBitmap(
                                            requireContext().getContentResolver(),
                                            filePath
                                    );
                            binding.profileImageView.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        uploadImage();
                    }
                }
            }
    );

    private void loadUserInfo() {
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String username = snapshot.child("username").getValue().toString();
                        String profileImage = snapshot.child("profileImage").getValue().toString();
                        String userMail = snapshot.child("email").getValue().toString();

                        binding.usernameTv.setText(username);
                        binding.usermailTv.setText(userMail);

                        if (!profileImage.isEmpty()) {
                            try {
                                Glide.with(getContext()).load(profileImage).into(binding.profileImageView);
                            } catch (Exception ignored) {
                                showToast(getString(R.string.some_error));
                            }
                        }
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        binding.content.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //do nothing
                    }
                });
    }

    private void loadFavorites() {
        database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    DataSnapshot userFavorites = snapshot.child("Users").child(currentUserId).child("favorites");
                    ArrayList<String> favorites = new ArrayList<>();
                    for (DataSnapshot child: userFavorites.getChildren()) {
                        String key = child.getKey();
                        favorites.add(key);
                    }
                    ArrayList<Attraction> attractions = new ArrayList<>();
                    for (String attractionId : favorites) {
                        DataSnapshot attractionSnapshot = snapshot.child("Attractions").child(attractionId);
                        String attraction_name = Objects.requireNonNull(attractionSnapshot.child("title").getValue()).toString();
                        String attraction_rating = Objects.requireNonNull(attractionSnapshot.child("rating").getValue()).toString();
                        String attraction_imgPoster = Objects.requireNonNull(attractionSnapshot.child("imgPoster").getValue()).toString();
                        attractions.add(new Attraction(attractionId, attraction_name, attraction_rating, attraction_imgPoster));
                    }

                    binding.favoritesRv.setLayoutManager(new LinearLayoutManager(getContext()));
                    if (attractions.isEmpty()) {
                        binding.favoritesTitle.setText(R.string.no_favorites_title);
                    } else {
                        binding.favoritesRv.setAdapter(new AttractionAdapter(attractions));
                    }
                } catch (Exception ignored) {
                    showToast(getString(R.string.data_upload_error));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //do nothing
            }
        });
    }

    private void selectImage() {
        try {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            pickImageActivityResultLauncher.launch(intent);
        } catch (Exception ignored) {
            showToast(getString(R.string.some_error));
        }
    }

    private void uploadImage() {
        if (filePath != null) {
            String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

            FirebaseStorage.getInstance().getReference().child("images/" + uid)
                    .putFile(filePath).addOnSuccessListener(taskSnapshot -> {
                        showToast(getString(R.string.photo_upload_complete));

                        FirebaseStorage.getInstance().getReference().child("images/" + uid).getDownloadUrl()
                                .addOnSuccessListener(uri -> FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("profileImage").setValue(uri.toString()));
                    });
        }
    }

    private void showToast(String text) {
        try {
            if (isAdded()) Toast.makeText(requireActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            // do nothing
        }
    }
}
