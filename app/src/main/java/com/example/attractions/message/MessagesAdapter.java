package com.example.attractions.message;


import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Objects;

import com.example.attractions.R;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>{

    private List<Message> messages;

    public MessagesAdapter (List<Message> messages){
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);

        if (message.getText() == null) {
            String photoUrl = message.getPhotoUrl();
            Glide.with(holder.itemView.getContext()).load(photoUrl).into(holder.photoImageView);
            holder.messageTv.setVisibility(View.GONE);
        } else {
            holder.messageTv.setText(message.getText());
            holder.photoImageView.setVisibility(View.GONE);
        }
        holder.dateTv.setText(message.getDate());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getOwnerId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()))
            return R.layout.message_from_curr_user_rv_item;
        else
            return R.layout.message_rv_item;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder{

        TextView messageTv, dateTv;

        ImageView photoImageView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            photoImageView = itemView.findViewById(R.id.photo_image_view);
            messageTv = itemView.findViewById(R.id.message_tv);
            dateTv = itemView.findViewById(R.id.message_date_tv);
        }
    }
}
