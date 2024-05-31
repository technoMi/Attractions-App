package com.example.attractions;

import static java.security.AccessController.getContext;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

import com.example.attractions.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.signUpBtn.setOnClickListener(v -> {
            if (binding.emailEt.getText().toString().isEmpty() || binding.passwordEt.getText().toString().isEmpty()
                || binding.usernameEt.getText().toString().isEmpty()){
                Toast.makeText(getApplicationContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            }else{
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(binding.emailEt.getText().toString(), binding.passwordEt.getText().toString())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                HashMap<String, String> userInfo = new HashMap<>();
                                userInfo.put("email", binding.emailEt.getText().toString());
                                userInfo.put("username", binding.usernameEt.getText().toString());
                                userInfo.put("profileImage", "");
                                userInfo.put("chats", "");

                                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(userInfo);

                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            } else {
                                Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}