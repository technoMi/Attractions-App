package com.example.attractions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.attractions.databinding.NewReviewActivityBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.spec.ECField;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class NewReviewActivity extends AppCompatActivity {

    private NewReviewActivityBinding binding;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private String attractionId;

    private final String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    private final String currentUserEmail = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();

    private Uri filePath;

    private String uniqueHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = NewReviewActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        attractionId = getIntent().getStringExtra("attractionId");

        binding.leaveReviewButton.setOnClickListener(v -> {
            if (filedValidationCheck()) {
                try {
                    String userText = String.valueOf(Objects.requireNonNull(binding.textInputLayout.getEditText()).getText());
                    Boolean isAnonymousReview = binding.doNotNameShowButton.isChecked();

                    database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            DataSnapshot data = snapshot.child("Reviews").child(attractionId);
                            DataSnapshot userInfo = snapshot.child("Users").child(currentUserId);
                            DatabaseReference reference;

                            assert currentUserEmail != null;
                            uniqueHash = String.valueOf(currentUserEmail.hashCode());
                            reference = data.child(uniqueHash).getRef();
                            reference.setValue(uniqueHash);

                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
                            String date = simpleDateFormat.format(new Date());

                            reference.child("text").setValue(userText);
                            reference.child("date").setValue(date);
                            if (!isAnonymousReview) {
                                reference.child("username").setValue(userInfo.child("username").getValue(String.class));
                                reference.child("profileImageUrl").setValue(userInfo.child("profileImage").getValue(String.class));
                            }
                            if (filePath != Uri.EMPTY && filePath != null) {
                                try {
                                    uploadImage();
                                    reference.child("photoUrl").setValue(currentUserId+attractionId);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // do nothing
                        }
                    });
                    showToast("Отзыв успешно сохранён");
                    finish();
                } catch (Exception ignored) {
                    showToast("Ошибка. Повторите попытку позже...");
                }
            } else {
                binding.textInputLayout.setError("Поле не может быть пустым!");
            }
        });

        binding.addPhotoButton.setOnClickListener(v -> selectImage());
        binding.backBtn.setOnClickListener(v -> finish());
        binding.closeViewButton.setOnClickListener(v -> {
            filePath = Uri.EMPTY;
            changeAddPhotoButtonState();
        });
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
                                            getApplicationContext().getContentResolver(),
                                            filePath
                                    );
                            binding.imageView.setImageBitmap(bitmap);
                            changeAddPhotoButtonState();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

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

    private void uploadImage() throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos); // Качество сжатия 50%
        byte[] data = baos.toByteArray();

        FirebaseStorage.getInstance().getReference().child("reviewsPhotos/" + currentUserId + attractionId)
                .putBytes(data).addOnSuccessListener(taskSnapshot -> {
                    FirebaseStorage.getInstance().getReference().child("reviewsPhotos/" + currentUserId + attractionId).getDownloadUrl()
                            .addOnSuccessListener(uri -> FirebaseDatabase.getInstance()
                                    .getReference()
                                    .child("Reviews")
                                    .child(attractionId)
                                    .child(String.valueOf(currentUserEmail.hashCode()))
                                    .child("photoUrl")
                                    .setValue(uri.toString()));
                });
    }


    private boolean filedValidationCheck() {
        return Objects.requireNonNull(binding.textInputLayout.getEditText()).length() != 0;
    }

    private void changeAddPhotoButtonState() {
        if (filePath.equals(Uri.EMPTY)) {
            binding.addPhotoButton.setText("+");
            binding.closeViewButton.setVisibility(View.GONE);
            binding.imageView.setVisibility(View.GONE);
            binding.addPhotoButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.baseline_add_a_photo_24, 0);
        } else {
            binding.addPhotoButton.setText("");
            binding.closeViewButton.setVisibility(View.VISIBLE);
            binding.imageView.setVisibility(View.VISIBLE);
            binding.addPhotoButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.baseline_autorenew_24, 0);
        }
    }

    private void showToast(String text) {
        try {
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            // do nothing
        }
    }
}
