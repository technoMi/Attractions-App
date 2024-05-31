package com.example.attractions.bottomnav.new_chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import com.example.attractions.databinding.FragmentNewChatBinding;
import com.example.attractions.users.User;
import com.example.attractions.users.UsersAdapter;

public class NewChatFragment extends Fragment {
    private FragmentNewChatBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNewChatBinding.inflate(inflater, container, false);

        loadUsers();

        return binding.getRoot();
    }

    private void loadUsers(){
        ArrayList<User> users = new ArrayList<User>();
        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()){
                        if (userSnapshot.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            continue;
                        }

                        String uid = userSnapshot.getKey();
                        String username = userSnapshot.child("username").getValue().toString();
                        String profileImage = userSnapshot.child("profileImage").getValue().toString();

                        users.add(new User(uid, username, profileImage));
                    }

                    binding.usersRv.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.usersRv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
                    binding.usersRv.setAdapter(new UsersAdapter(users, getContext()));
                    if (users.isEmpty()) {
                        binding.thereIsNothingLabel.setVisibility(View.VISIBLE);
                    }
                } catch (NullPointerException ignored) {
                    Toast.makeText(getContext(), "Error. Try again later.", Toast.LENGTH_SHORT).show();
                }
                binding.progressBar2.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
