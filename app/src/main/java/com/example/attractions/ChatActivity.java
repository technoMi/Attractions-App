package com.example.attractions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.example.attractions.databinding.ActivityChatBinding;
import com.example.attractions.message.Message;
import com.example.attractions.message.MessagesAdapter;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;

    private boolean isConversationCreatedForInterlocutor = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String chatId = getIntent().getStringExtra("chatId");
        String interlocutorName = getIntent().getStringExtra("interlocutorName");
        String interlocutorProfileImage = getIntent().getStringExtra("interlocutorProfileImage");

        loadInterlocutorInfo(interlocutorName, interlocutorProfileImage);

        loadMessages(chatId);

        binding.sendMessageBtn.setOnClickListener(v -> {
            String message = binding.messageEt.getText().toString();
            if (message.isEmpty()){
                Toast.makeText(this, getString(R.string.empty_message_error), Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            String date = simpleDateFormat.format(new Date());

            binding.messageEt.setText(""); //clearing the edit text
            sendMessage(chatId, message, date);
        });
    }

    private void sendMessage(String chatId, String message, String date){
        if (chatId==null) return;

        HashMap<String, String> messageInfo = new HashMap<>();
        messageInfo.put("text", message);
        messageInfo.put("ownerId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        messageInfo.put("date", date);

        FirebaseDatabase.getInstance().getReference().child("Chats").child(chatId)
                .child("messages").push().setValue(messageInfo);
        if (!isConversationCreatedForInterlocutor) {
            
        }
    }

    private void loadInterlocutorInfo(String name, String imgUrl) {
        if (!imgUrl.isEmpty())
            Glide.with(getApplicationContext()).load(imgUrl).into(binding.profileIv);
        if (!name.isEmpty())
            binding.usernameTv.setText(name);
    }

    private void loadMessages(String chatId){
        if (chatId==null) return;

        FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(chatId).child("messages").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            binding.textView.setText(getString(R.string.lets_start_new_conversation));
                            return;
                        } else {
                            binding.textView.setVisibility(View.INVISIBLE);
                            isConversationCreatedForInterlocutor = true;
                        }

                        List<Message> messages = new ArrayList<>();
                        for (DataSnapshot messageSnapshot : snapshot.getChildren()){
                            String messageId = messageSnapshot.getKey();
                            String ownerId = Objects.requireNonNull(messageSnapshot.child("ownerId").getValue()).toString();
                            String text = Objects.requireNonNull(messageSnapshot.child("text").getValue()).toString();
                            String date = Objects.requireNonNull(messageSnapshot.child("date").getValue()).toString();

                            messages.add(new Message(messageId, ownerId, text, date));
                        }

                        LinearLayoutManager llm = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false);
                        llm.setStackFromEnd(true);
                        binding.messagesRv.setLayoutManager(llm);
                        binding.messagesRv.setAdapter(new MessagesAdapter(messages));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // do nothing
                    }
                });
    }
}