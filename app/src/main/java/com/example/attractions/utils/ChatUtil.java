package com.example.attractions.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import com.example.attractions.users.User;
import com.google.firebase.database.ValueEventListener;

public class ChatUtil {

    public static void createChat(User user, Context context) {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String chatId = generateChatId(uid, user.uid);

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("Chats").child(chatId);
        chatRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    Toast.makeText(context, "Чат с этим пользователем уже существует!", Toast.LENGTH_SHORT).show();
                } else {
                    HashMap<String, String> chatInfo = new HashMap<>();
                    chatInfo.put("user1", uid);
                    chatInfo.put("user2", user.uid);

                    chatRef.setValue(chatInfo);
                    addChatIdToUser(uid, chatId);
                    //addChatIdToUser(user.uid, chatId);
                    Toast.makeText(context, "Создан новый чат", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void createChatForInterlocutor(User user, Context context) {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String chatId = generateChatId(uid, user.uid);

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("Chats").child(chatId);
        chatRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
//                HashMap<String, String> chatInfo = new HashMap<>();
//                chatInfo.put("user1", uid);
//                chatInfo.put("user2", user.uid);

                //chatRef.setValue(chatInfo);
                //addChatIdToUser(uid, chatId);
                addChatIdToUser(user.uid, chatId);
            }
        });
    }

    private static String generateChatId(String userId1, String userId2) {
        String sumUser1User2 = userId1 + userId2;
        char[] charArray = sumUser1User2.toCharArray();
        Arrays.sort(charArray);

        return new String(charArray);
    }

    private static void addChatIdToUser(String uid, String chatId) {
        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot chatsSnapshot = snapshot
                        .child("Users")
                        .child(uid)
                        .child("chats");

                DatabaseReference chatsReference = snapshot
                        .child("Users")
                        .child(uid)
                        .child("chats")
                        .getRef();

                String chats;

                chats = chatsSnapshot.getValue(String.class);
                if (chats == null) chats = "";
                String chatsUpd = addIdToStr(chats, chatId);

                chatsReference.setValue(chatsUpd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // do nothing
            }
        });
    }

    private static String addIdToStr(String str, String chatId) {
        str += (str.isEmpty()) ? chatId : ("," + chatId);
        return str;
    }
}
