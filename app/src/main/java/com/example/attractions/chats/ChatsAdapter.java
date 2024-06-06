package com.example.attractions.chats;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

import com.example.attractions.ChatActivity;
import com.example.attractions.R;
import com.google.firebase.database.ValueEventListener;

public class ChatsAdapter extends RecyclerView.Adapter<ChatViewHolder>{

    private final ArrayList<Chat> chats;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    private final FirebaseAuth databaseAuth = FirebaseAuth.getInstance();

    public ChatsAdapter(ArrayList<Chat> chats){
        this.chats = chats;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.person_item_rv, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.chat_name_tv.setText(chats.get(position).getChat_name());

        String userId;
        if (!chats.get(position).getUserId1().equals(Objects.requireNonNull(databaseAuth.getCurrentUser()).getUid())){
            userId = chats.get(position).getUserId1();
        }else{
            userId = chats.get(position).getUserId2();
        }

        database.getReference().child("Users").child(userId)
                .child("profileImage").get()
                .addOnCompleteListener(task -> {
                    try{
                        String profileImageUrl = Objects.requireNonNull(task.getResult().getValue()).toString();
                        if (!profileImageUrl.isEmpty())
                            Glide.with(holder.itemView.getContext()).load(profileImageUrl).into(holder.chat_iv);
                    }catch(Exception e){
                        Toast.makeText(holder.itemView.getContext(), "Ошибка получения фотографии.", Toast.LENGTH_SHORT).show();
                    }
                });


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ChatActivity.class);
            database.getReference("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String interlocutorName = dataSnapshot.child("username").getValue(String.class);
                            String interlocutorProfileImage = dataSnapshot.child("profileImage").getValue(String.class);

                            intent.putExtra("interlocutorName", interlocutorName);
                            intent.putExtra("interlocutorProfileImage", interlocutorProfileImage);
                            intent.putExtra("chatId", chats.get(position).getChat_id());

                            holder.itemView.getContext().startActivity(intent);
                        } else {
                            Toast.makeText(holder.itemView.getContext(), "Ошибка получения данных. Повторите попытку позже...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // do nothing
                    }
                });
            });
        }

    @Override
    public int getItemCount() {
        return chats.size();
    }
}
