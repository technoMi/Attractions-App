package com.example.attractions.bottomnav.chats;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import com.example.attractions.chats.Chat;
import com.example.attractions.chats.ChatsAdapter;
import com.example.attractions.databinding.FragmentChatsBinding;

public class ChatsFragment extends Fragment {
    private FragmentChatsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);

        loadChats();

        return binding.getRoot();
    }

    private void loadChats(){
        ArrayList<Chat> chats = new ArrayList<>();

        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    String chatsStr = Objects.requireNonNull(snapshot.child("Users").child(uid).child("chats").getValue()).toString();
                    String[] chatsIds = chatsStr.split(",");
                    if (chatsIds.length==0) return;

                    for (String chatId : chatsIds){
                        DataSnapshot chatSnapshot = snapshot.child("Chats").child(chatId);
                        String userId1 = Objects.requireNonNull(chatSnapshot.child("user1").getValue()).toString();
                        String userId2 = Objects.requireNonNull(chatSnapshot.child("user2").getValue()).toString();

                        String chatUserId = (uid.equals(userId1)) ? userId2 : userId1;
                        String chatName = Objects.requireNonNull(snapshot.child("Users").child(chatUserId).child("username").getValue()).toString();

                        Chat chat = new Chat(chatId, chatName, userId1, userId2);
                        chats.add(chat);
                    }
                } catch (NullPointerException ignored) {
                    // do nothing
                }
                if (chats.isEmpty()) {
                    binding.thereIsNothingLabel.setVisibility(View.VISIBLE);
                } else {
                    binding.chatsRv.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.chatsRv.setAdapter(new ChatsAdapter(chats));
                }
                binding.progressBar2.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to get user chats");
            }

            private void showToast(String text) {
                try {
                    if (isAdded()) Toast.makeText(requireActivity().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                } catch (NullPointerException e) {
                    // do nothing
                }
            }
        });
    }
}
