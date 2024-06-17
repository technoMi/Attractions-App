package com.example.attractions;

import static com.example.attractions.utils.ChatUtil.createChatForInterlocutor;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.attractions.users.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.example.attractions.databinding.ActivityChatBinding;
import com.example.attractions.message.Message;
import com.example.attractions.message.MessagesAdapter;
import com.google.firebase.storage.FirebaseStorage;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;

    private String interlocutorId;
    private String interlocutorName;
    private String interlocutorProfileImage;

    private String chatId;

    private Uri filePath;

    private boolean isConversationCreatedForInterlocutor = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        interlocutorId = getIntent().getStringExtra("interlocutorId");
        chatId = getIntent().getStringExtra("chatId");
        interlocutorName = getIntent().getStringExtra("interlocutorName");
        interlocutorProfileImage = getIntent().getStringExtra("interlocutorProfileImage");

        assert interlocutorProfileImage != null;
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

        binding.addImageButton.setOnClickListener(v -> selectImage());
    }

    ActivityResultLauncher<Intent> pickImageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        filePath = result.getData().getData();
                    }
                    try {
                        uploadImage();
                    } catch (IOException e) {
                        showToast(getString(R.string.some_error));
                    }
                }
            }
    );

    private void uploadImage() throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
        byte[] data = baos.toByteArray();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        String date = simpleDateFormat.format(new Date());

        String milliTime = String.valueOf(System.currentTimeMillis());

        FirebaseStorage.getInstance().getReference().child("chatsImages/" + chatId.hashCode() + milliTime)
                .putBytes(data).addOnCompleteListener(taskSnapshot ->
                        FirebaseStorage.getInstance().getReference().child("chatsImages/" + chatId.hashCode() + milliTime).getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                        sendPhoto(chatId, uri, date)
                                )
                );
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

    private void sendPhoto(String chatId, Uri uri, String date) {
        if (chatId==null) return;

        HashMap<String, String> messageInfo = new HashMap<>();
        messageInfo.put("photoUrl", String.valueOf(uri));
        messageInfo.put("ownerId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        messageInfo.put("date", date);

        FirebaseDatabase.getInstance().getReference().child("Chats").child(chatId)
                .child("messages").push().setValue(messageInfo);

        if (!isConversationCreatedForInterlocutor) {
            User interlocutor = new User(interlocutorId, interlocutorName, interlocutorProfileImage);
            createChatForInterlocutor(
                    interlocutor,
                    getApplicationContext());
        }
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
            User interlocutor = new User(interlocutorId, interlocutorName, interlocutorProfileImage);
            createChatForInterlocutor(
                    interlocutor,
                    getApplicationContext());
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

                            String text, photoUrl;
                            try {
                                text = Objects.requireNonNull(messageSnapshot.child("text").getValue()).toString();
                                photoUrl = null;
                            } catch (NullPointerException e) {
                                photoUrl = Objects.requireNonNull(messageSnapshot.child("photoUrl").getValue()).toString();
                                text = null;
                            }

                            String date = Objects.requireNonNull(messageSnapshot.child("date").getValue()).toString();

                            messages.add(new Message(messageId, ownerId, text, photoUrl, date));
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

    private void showToast(String text) {
        try {
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            // do nothing
        }
    }
}