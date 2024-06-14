package com.example.attractions.reviews;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.attractions.R;
import com.example.attractions.message.Message;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>{

    private final List<Review> reviews;

    public ReviewsAdapter(List<Review> reviews){
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);

        holder.text.setText(review.getText());
        holder.date.setText(review.getDate());

        if (!review.username.isEmpty()){
            if (review.profileImageUrl != null) {
                Glide.with(holder.itemView.getContext()).load(review.profileImageUrl).into(holder.profilePhoto);
            }
        }

        if (!review.photoUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(review.photoUrl).into(holder.attachedPhoto);
            holder.attachedPhoto.setVisibility(View.VISIBLE);
        }

//        holder.profilePhoto.setText(review.getDate());
//        holder.date.setText(review.getDate());
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.review_item;
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder{

        TextView date, text, username;

        CircleImageView profilePhoto;

        ImageView attachedPhoto;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.review_date);
            attachedPhoto = itemView.findViewById(R.id.review_photo);
            profilePhoto = itemView.findViewById(R.id.review_profile_iv);
            text = itemView.findViewById(R.id.review_text);
            username = itemView.findViewById(R.id.review_username);
        }
    }
}
